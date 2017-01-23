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

package com.dangdang.ddframe.job.example;

import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.example.listener.JavaSimpleDistributeListener;
import com.dangdang.ddframe.job.example.listener.JavaSimpleListener;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.internal.config.LiteJobConfigurationGsonFactory;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractJobListener;
import com.dangdang.ddframe.job.lite.internal.listener.AbstractListenerManager;
import com.dangdang.ddframe.job.lite.internal.schedule.JobRegistry;
import com.dangdang.ddframe.job.lite.internal.schedule.JobScheduleController;
import com.dangdang.ddframe.job.lite.internal.server.ServerService;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.base.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;

import javax.sql.DataSource;

import static com.dangdang.ddframe.job.example.CommonConfig.*;

@Slf4j
public final class AutoJobJavaMain {

    public static void main(String[] args) {

        CoordinatorRegistryCenter regCenter = setUpRegistryCenter();
        JobEventConfiguration jobEventConfig = new JobEventRdbConfiguration(setUpEventTraceDataSource());

        new JobModifyListenerManager(regCenter, jobEventConfig, jobCreateConfigNode).start();
        while (true) {
            try {
                Thread.sleep(1000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private static class JobModifyListenerManager extends AbstractListenerManager {

        private final CoordinatorRegistryCenter regCenter;
        private final JobEventConfiguration jobEventConfig;
        private final String prefix;
        private final String suffix;

        public JobModifyListenerManager(final CoordinatorRegistryCenter regCenter, final JobEventConfiguration jobEventConfig, final String configRootNode) {
            super(regCenter, configRootNode);
            this.regCenter = regCenter;
            this.jobEventConfig = jobEventConfig;
            regCenter.addCacheData("/" + configRootNode);
            prefix = "/" + configRootNode;
            suffix = "/config";
        }

        @Override
        public void start() {
            addDataListener(new JobModifyListener());
        }

        class JobModifyListener extends AbstractJobListener {

            @Override
            protected void dataChanged(final CuratorFramework client, final TreeCacheEvent event, final String path) {
                if ((TreeCacheEvent.Type.NODE_ADDED == event.getType() || TreeCacheEvent.Type.NODE_REMOVED == event.getType() || TreeCacheEvent.Type.NODE_UPDATED == event.getType())
                        && path.startsWith(prefix)
                        && path.endsWith(suffix)
                        && path.length() > (prefix.length() + suffix.length())) {
                    String jobName = path.substring(prefix.length() + 1, path.lastIndexOf(suffix));

                    if (TreeCacheEvent.Type.NODE_ADDED == event.getType() || TreeCacheEvent.Type.NODE_UPDATED == event.getType()) {

                        String configData = regCenter.get(path);
                        if (StringUtils.isBlank(configData)) {
                            return;
                        }

                        if (!regCenter.isExisted("/" + jobName)) {
                            log.info("create a new scheduling task:" + jobName);
                            LiteJobConfiguration liteJobConfiguration = LiteJobConfigurationGsonFactory.fromJson(configData);
                            log.info("liteJobConfiguration:" + configData);
                            runJob(liteJobConfiguration, regCenter, jobEventConfig);
                        } else {
                            log.info("timed scheduling tasks have been completed or are being executed:" + jobName);
                        }
                    } else {
                        JobScheduleController jobScheduleController = JobRegistry.getInstance().getJobScheduleController(jobName);
                        if (jobScheduleController != null) {
                            log.info("schedule task exists, delete:" + jobName);
                            jobScheduleController.shutdown();
                            ServerService serverService = new ServerService(regCenter, jobName);
                            serverService.processServerShutdown();

                            if (regCenter.isExisted("/" + jobName)) {
                                regCenter.remove("/" + jobName);
                            }
                        } else {
                            log.info("time scheduling task does not exist:" + jobName);
                        }

                        if (regCenter.isExisted("/" + jobName)) {
                            log.info("delete job node:" + jobName);
                            regCenter.remove("/" + jobName);
                        } else {
                            log.info("job node does not exist:" + jobName);
                        }

                    }
                }
            }
        }
    }


    public static void runJob(final LiteJobConfiguration liteJobConfiguration, final CoordinatorRegistryCenter regCenter, final JobEventConfiguration jobEventConfig) {
        setUpSimpleJob(liteJobConfiguration, regCenter, jobEventConfig);
    }

    private static CoordinatorRegistryCenter setUpRegistryCenter() {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(ZOOKEEPER_CONNECTION_STRING, JOB_NAMESPACE);
        Optional<String> optional = Optional.fromNullable(digest);
        if (optional.isPresent()) {
            zkConfig.setDigest(digest);
        }
        CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(zkConfig);
        result.init();
        return result;
    }

    private static DataSource setUpEventTraceDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(EVENT_RDB_STORAGE_DRIVER);
        result.setUrl(EVENT_RDB_STORAGE_URL);
        result.setUsername(EVENT_RDB_STORAGE_USERNAME);
        result.setPassword(EVENT_RDB_STORAGE_PASSWORD);
        return result;
    }

    private static void setUpSimpleJob(final LiteJobConfiguration liteJobConfiguration, final CoordinatorRegistryCenter regCenter, final JobEventConfiguration jobEventConfig) {
        new JobScheduler(regCenter, liteJobConfiguration, jobEventConfig, new JavaSimpleListener(), new JavaSimpleDistributeListener(1000L, 2000L)).init();
    }

}
