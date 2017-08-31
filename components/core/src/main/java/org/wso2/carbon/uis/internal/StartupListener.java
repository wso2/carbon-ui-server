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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uis.api.deployment.Server;
import org.wso2.carbon.uis.api.http.HttpConnector;
import org.wso2.carbon.uis.internal.deployment.AppFinder;
import org.wso2.carbon.uis.internal.http.OsgiHttpConnector;
import org.wso2.carbon.uis.internal.io.ArtifactAppFinder;

import java.nio.file.Paths;

@Component(name = "org.wso2.carbon.uis.internal.StartupListener",
           immediate = true
)
public class StartupListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartupListener.class);

    private BundleContext bundleContext;
    private Server server;

    @Activate
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        AppFinder appFinder = new ArtifactAppFinder(
                Paths.get(System.getProperty("carbon.home", "."), "deployment", "reactapps"));
        HttpConnector httpConnector = new OsgiHttpConnector(this.bundleContext);
        this.server = new Server(appFinder, httpConnector);
        this.server.start();
        LOGGER.debug("Carbon UI Server activated.");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        this.bundleContext = null;
        this.server.stop();
        this.server = null;
        LOGGER.debug("Carbon UI Server deactivated.");
    }
}
