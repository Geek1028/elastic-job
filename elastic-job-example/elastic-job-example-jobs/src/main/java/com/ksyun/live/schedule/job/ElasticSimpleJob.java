/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.ksyun.live.schedule.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.example.http.CommonUtil;
import com.dangdang.ddframe.job.example.http.CreateJobMeta;
import com.dangdang.ddframe.job.example.http.RestClient;
import com.dangdang.ddframe.job.example.http.RestResult;
import com.dangdang.ddframe.job.util.env.HostException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.util.Base64Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class ElasticSimpleJob implements SimpleJob {

    @Override
    public void execute(final ShardingContext shardingContext) {
        log.info("ElasticSimpleJob.execute:" + shardingContext.toString());
        String shardingParameter = shardingContext.getShardingParameter();
        CreateJobMeta jobMeta = null;
        if (!StringUtils.isBlank(shardingParameter)) {
            try {
                shardingParameter = new String(Base64Utils.decodeFromString(shardingParameter), "UTF-8");
                jobMeta = CommonUtil.json2object(shardingParameter, CreateJobMeta.class);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (null != jobMeta) {
            log.info("jobMeta:" + jobMeta.toString());
            String callback_url = null;
            try {
                callback_url = URLDecoder.decode(jobMeta.getCallback_url(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String callback_data = jobMeta.getCallback_data();
            HttpMethod callback_method = jobMeta.getCallback_method();
            Integer type = jobMeta.getType();
            String cron = jobMeta.getCron();
            String data = jobMeta.getData();

            Map<String, String> headers = new LinkedHashMap<String, String>();
            headers.put("jobName", shardingContext.getJobName());
            headers.put("jobType", String.valueOf(type));
            headers.put("jobStartTime", String.valueOf(System.currentTimeMillis()));
            headers.put("jobHostname", getHostName());
            headers.put("jobCron", cron);
            headers.put("jobInfoData", data);
            headers.put("jobCallbackData", callback_data);
            RestResult<String> response = RestClient.getInstance().execute(callback_method, callback_url, null, headers, String.class, "application/json");
            if (response != null && 2 == (response.getStatus() / 100)) {
                log.info(response.toString());
            }
        }
    }

    public String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException ex) {
            throw new HostException(ex);
        }
    }
}
