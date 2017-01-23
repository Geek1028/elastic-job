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

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.example.job.simple.JavaSimpleJob;
import com.dangdang.ddframe.job.example.listener.JavaSimpleDistributeListener;
import com.dangdang.ddframe.job.example.listener.JavaSimpleListener;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.base.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;

import static com.dangdang.ddframe.job.example.CommonConfig.*;

@Slf4j
public final class JavaMain {

    public static void main(String[] args) {
        System.out.println(JavaSimpleJob.class.getCanonicalName());

//        String cron = "0 47 16 20 1 ? 2017";
//
//        if (args != null && args.length > 0) {
//            cron = args[0];
//        }
//
//        CoordinatorRegistryCenter regCenter = setUpRegistryCenter();
//        JobEventConfiguration jobEventConfig = new JobEventRdbConfiguration(setUpEventTraceDataSource());
//
//        setUpSimpleJob("test123", regCenter, jobEventConfig, cron);

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

    private static void setUpSimpleJob(final String jobName, final CoordinatorRegistryCenter regCenter, final JobEventConfiguration jobEventConfig, final String cron) {
        JobCoreConfiguration coreConfig = JobCoreConfiguration.newBuilder(jobName, cron, 3).shardingItemParameters("0=Beijing,1=Shanghai,2=Guangzhou").failover(true).build();
        SimpleJobConfiguration simpleJobConfig = new SimpleJobConfiguration(coreConfig, JavaSimpleJob.class.getCanonicalName());
        new JobScheduler(regCenter, LiteJobConfiguration.newBuilder(simpleJobConfig).overwrite(true).build(), jobEventConfig, new JavaSimpleListener(), new JavaSimpleDistributeListener(1000L, 2000L)).init();
    }

}
