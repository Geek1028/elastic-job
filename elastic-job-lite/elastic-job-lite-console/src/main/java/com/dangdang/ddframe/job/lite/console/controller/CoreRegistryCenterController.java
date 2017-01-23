package com.dangdang.ddframe.job.lite.console.controller;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.console.domain.CreateJobMeta;
import com.dangdang.ddframe.job.lite.console.domain.DeleteJobMeta;
import com.dangdang.ddframe.job.lite.console.domain.RegistryCenterConfiguration;
import com.dangdang.ddframe.job.lite.console.service.RegistryCenterService;
import com.dangdang.ddframe.job.lite.internal.config.LiteJobConfigurationGsonFactory;
import com.dangdang.ddframe.job.lite.lifecycle.internal.reg.RegistryCenterFactory;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.util.json.GsonFactory;
import com.google.common.base.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by liuheng3 on 2017/1/22.
 */
@RestController
@RequestMapping("job")
@Slf4j
public class CoreRegistryCenterController {

    @Resource
    private RegistryCenterService regCenterService;

    @RequestMapping(value = "create", method = RequestMethod.POST)
    @ResponseBody
    public Map createJob(@RequestBody @Valid final CreateJobMeta createJobMeta) throws UnsupportedEncodingException {
        log.info("createJob:" + createJobMeta.toString());
        Map<String, String> jobInfo = new HashMap<String, String>();

        RegistryCenterConfiguration registryCenterConfiguration = regCenterService.load(createJobMeta.getRegistryCenterName());
        if (null != registryCenterConfiguration) {
            CoordinatorRegistryCenter registryCenter = RegistryCenterFactory.createCoordinatorRegistryCenter(registryCenterConfiguration.getZkAddressList(), registryCenterConfiguration.getNamespace(), Optional.fromNullable(registryCenterConfiguration.getDigest()));
            String jobName = UUID.randomUUID().toString();
            String jobConfigNode = "";
            while (true) {
                jobConfigNode = String.format("/%s/%s/%s", "jobCreateConfigNode", jobName, "config");
                if (!registryCenter.isExisted(jobConfigNode)) {
                    break;
                }
                jobName = UUID.randomUUID().toString();
            }

            String parameter = GsonFactory.getGson().toJson(createJobMeta);
            List<String> parameters = new ArrayList<String>();
            for (int i = 0; i < createJobMeta.getShardingTotalCount(); i++) {
                parameters.add(String.format("%s=%s", i, Base64Utils.encodeToString(parameter.getBytes("UTF-8"))));
            }
            String shardingItemParameters = StringUtils.join(parameters, ",");

            JobCoreConfiguration coreConfig = JobCoreConfiguration.newBuilder(jobName, createJobMeta.getCron(), createJobMeta.getShardingTotalCount()).shardingItemParameters(shardingItemParameters).failover(true).misfire(true).build();
            SimpleJobConfiguration simpleJobConfig = new SimpleJobConfiguration(coreConfig, createJobMeta.getJobClass());
            LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration.newBuilder(simpleJobConfig).overwrite(true).build();

            String config = LiteJobConfigurationGsonFactory.toJson(liteJobConfiguration);
            log.info("config data:" + config);

            registryCenter.persist(jobConfigNode, config);

            jobInfo.put("jobName", jobName);

        }
        return jobInfo;
    }

    @RequestMapping(value = "delete", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteJob(@RequestBody final DeleteJobMeta deleteJobMeta) throws UnsupportedEncodingException {
        log.info("deleteJob:" + deleteJobMeta.toString());

        RegistryCenterConfiguration registryCenterConfiguration = regCenterService.load(deleteJobMeta.getRegistryCenterName());
        if (null != registryCenterConfiguration) {
            CoordinatorRegistryCenter registryCenter = RegistryCenterFactory.createCoordinatorRegistryCenter(registryCenterConfiguration.getZkAddressList(), registryCenterConfiguration.getNamespace(), Optional.fromNullable(registryCenterConfiguration.getDigest()));
            String jobConfigRootNode = String.format("/%s/%s", "jobCreateConfigNode", deleteJobMeta.getJobName());
            if (registryCenter.isExisted(jobConfigRootNode)) {
                registryCenter.remove(jobConfigRootNode);
                log.info("deleteJob success:" + deleteJobMeta.toString());
            } else {
                log.info("deleteJob fail,no exists:" + deleteJobMeta.toString());
            }
        }
    }

}
