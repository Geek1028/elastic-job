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

package com.dangdang.ddframe.job.lite.internal.listener;

import com.dangdang.ddframe.job.lite.internal.storage.JobNodeStorage;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionStateListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 作业注册中心的监听器管理者的抽象类.
 * 
 * @author zhangliang
 */
public abstract class AbstractListenerManager {
    
    private final JobNodeStorage jobNodeStorage;

    private List<ConnectionStateListener> connectionStateListeners;
    private List<TreeCacheListener> treeCacheListeners;

    protected AbstractListenerManager(final CoordinatorRegistryCenter regCenter, final String jobName) {
        jobNodeStorage = new JobNodeStorage(regCenter, jobName);
        connectionStateListeners = new ArrayList<ConnectionStateListener>();
        treeCacheListeners = new ArrayList<TreeCacheListener>();
    }

    /**
     * 开启监听器.
     */
    public abstract void start();

    public void close() {
        if (null != treeCacheListeners && treeCacheListeners.size() > 0) {
            for (TreeCacheListener treeCacheListener : treeCacheListeners) {
                jobNodeStorage.removeDataListener(treeCacheListener);
            }
        }

        if (null != connectionStateListeners && connectionStateListeners.size() > 0) {
            for (ConnectionStateListener connectionStateListener : connectionStateListeners) {
                jobNodeStorage.removeConnectionStateListener(connectionStateListener);
            }
        }
    }
    
    protected void addDataListener(final TreeCacheListener listener) {
        treeCacheListeners.add(listener);
        jobNodeStorage.addDataListener(listener);
    }
    
    protected void addConnectionStateListener(final ConnectionStateListener listener) {
        connectionStateListeners.add(listener);
        jobNodeStorage.addConnectionStateListener(listener);
    }
}
