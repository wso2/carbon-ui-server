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

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uis.api.App;
import org.wso2.carbon.uis.api.http.HttpConnector;
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
@Component(service = {Server.class, AppDeploymentEventListener.class},
           immediate = true)
public class CarbonUiServer implements Server, AppDeploymentEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarbonUiServer.class);

    private HttpConnector httpConnector;
    /**
     * Contains deployed apps. Here key is the app name and value is the deployed app.
     */
    private final ConcurrentMap<String, App> deployedApps = new ConcurrentHashMap<>();

    @Reference(service = HttpConnector.class,
               cardinality = ReferenceCardinality.MANDATORY,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetHttpConnector")
    protected void setHttpConnector(HttpConnector httpConnector) {
        this.httpConnector = httpConnector;
        LOGGER.debug("An instance of class '{}' registered as a HTTP connector to Carbon UI server.",
                     httpConnector.getClass().getName());
    }

    protected void unsetHttpConnector(HttpConnector httpConnector) {
        this.httpConnector = null;
        LOGGER.debug("An instance of class '{}' unregistered as a HTTP connector from Carbon UI server.",
                     httpConnector.getClass().getName());
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        LOGGER.debug("Carbon UI Server activated.");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        deployedApps.clear();
        httpConnector.unregisterAllApps();
        LOGGER.debug("Carbon UI Server deactivated.");
    }

    @Override
    public Optional<App> getApp(String appName) {
        return Optional.ofNullable(deployedApps.get(appName));
    }

    @Override
    public void appDeploymentEvent(App app) {
        deployedApps.put(app.getName(), app);
        RequestDispatcher requestDispatcher = new RequestDispatcher(app);
        httpConnector.registerApp(app, requestDispatcher::serve);
    }

    @Override
    public void appUndeploymentEvent(String appName) {
        deployedApps.remove(appName);
        httpConnector.unregisterApp(appName);
    }
}
