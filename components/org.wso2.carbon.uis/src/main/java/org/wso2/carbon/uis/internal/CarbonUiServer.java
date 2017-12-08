/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.uis.internal;

import org.wso2.carbon.uis.api.App;
import org.wso2.carbon.uis.internal.deployment.AppDeploymentEventListener;
import org.wso2.carbon.uis.spi.Server;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Carbon UI server service component.
 *
 * @since 0.8.0
 */
public class CarbonUiServer implements Server, AppDeploymentEventListener {

    /**
     * Contains deployed apps. Here key is the app name and value is the deployed app.
     */
    private final ConcurrentMap<String, App> deployedApps = new ConcurrentHashMap<>();

    @Override
    public Optional<App> getApp(String appName) {
        return Optional.ofNullable(deployedApps.get(appName));
    }

    @Override
    public void appDeploymentEvent(App app) {
        deployedApps.put(app.getName(), app);
    }

    @Override
    public void appUndeploymentEvent(String appName) {
        deployedApps.remove(appName);
    }

    public void close() {
        deployedApps.clear();
    }
}
