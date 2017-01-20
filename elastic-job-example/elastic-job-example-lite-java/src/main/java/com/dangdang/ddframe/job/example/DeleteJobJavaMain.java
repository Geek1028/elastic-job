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

import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public final class DeleteJobJavaMain {

    private static final int EMBED_ZOOKEEPER_PORT = 2181;

    private static final String ZOOKEEPER_CONNECTION_STRING = "10.64.7.106:" + EMBED_ZOOKEEPER_PORT;

    private static final String JOB_NAMESPACE = "elastic-job-example-lite-java";

    public static void main(String[] args) {


        CoordinatorRegistryCenter regCenter = setUpRegistryCenter();

        List<String> list = regCenter.getChildrenKeys("/jobCreateConfig");

        for (String str : list) {
            System.out.println("delete  " + str);

            if (regCenter.isExisted("/jobCreateConfig/" + str)) {
                regCenter.remove("/jobCreateConfig/" + str);
            }
        }
    }


    private static CoordinatorRegistryCenter setUpRegistryCenter() {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(ZOOKEEPER_CONNECTION_STRING, JOB_NAMESPACE);
        CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(zkConfig);
        result.init();
        return result;
    }

}
