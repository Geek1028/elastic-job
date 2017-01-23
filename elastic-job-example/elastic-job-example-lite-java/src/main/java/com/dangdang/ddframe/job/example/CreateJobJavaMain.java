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

import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.base.Optional;
import lombok.extern.slf4j.Slf4j;

import static com.dangdang.ddframe.job.example.CommonConfig.*;

@Slf4j
public final class CreateJobJavaMain {

    public static void main(String[] args) {


        CoordinatorRegistryCenter regCenter = setUpRegistryCenter();

        int index = 0;
        while (true) {
            String jobName = "test" + index;
            String cron = index + " 12 16 20 1 ? 2017";
            String config = "{\"jobName\":\"" + jobName + "\",\"jobClass\":\"com.dangdang.ddframe.job.example.job.simple.JavaSimpleJob\",\"jobType\":\"SIMPLE\",\"cron\":\"" + cron + "\",\"shardingTotalCount\":3,\"shardingItemParameters\":\"0\\u003dBeijing,1\\u003dShanghai,2\\u003dGuangzhou\",\"jobParameter\":\"\",\"failover\":true,\"misfire\":true,\"description\":\"\",\"jobProperties\":{\"job_exception_handler\":\"com.dangdang.ddframe.job.executor.handler.impl.DefaultJobExceptionHandler\",\"executor_service_handler\":\"com.dangdang.ddframe.job.executor.handler.impl.DefaultExecutorServiceHandler\"},\"monitorExecution\":true,\"maxTimeDiffSeconds\":-1,\"monitorPort\":-1,\"jobShardingStrategyClass\":\"\",\"disabled\":false,\"overwrite\":true}";
            System.out.println(config);

            JobNodeStorage jobNodeStorage = new JobNodeStorage(regCenter, jobCreateConfigNode);

            jobNodeStorage.createJobNodeIfNeeded(jobName + "/config");
            jobNodeStorage.fillJobNode(jobName + "/config", config);
            try {
                Thread.sleep(1000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            index++;
            if (index >= 60) {
                break;
            }
        }


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

}
