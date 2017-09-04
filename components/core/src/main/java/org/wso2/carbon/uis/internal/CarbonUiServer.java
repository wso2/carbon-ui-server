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
import org.wso2.carbon.uis.api.Extension;
import org.wso2.carbon.uis.api.http.HttpConnector;
import org.wso2.carbon.uis.api.http.HttpRequest;
import org.wso2.carbon.uis.api.http.HttpResponse;
import org.wso2.carbon.uis.internal.deployment.AppRegistry;
import org.wso2.carbon.uis.internal.io.ArtifactAppFinder;
import org.wso2.carbon.uis.spi.Server;

import java.util.Map;
import java.util.function.Function;

/**
 * Carbon UI server service component.
 *
 * @since 0.8.0
 */
@Component(name = "org.wso2.carbon.uis.internal.CarbonUiServer",
           service = Server.class,
           immediate = true,
           property = {
                   "componentName=wso2-carbon-ui-server"
           }
)
public class CarbonUiServer implements Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarbonUiServer.class);

    private final AppRegistry appRegistry;
    private HttpConnector httpConnector;
    private Function<HttpRequest, HttpResponse> httpListener;
    private BundleContext bundleContext;

    public CarbonUiServer() {
        this.appRegistry = new AppRegistry(new ArtifactAppFinder());
    }

    @Reference(name = "httpConnector",
               service = HttpConnector.class,
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
        this.bundleContext = bundleContext;
        RequestDispatcher requestDispatcher = new RequestDispatcher();
        this.httpListener = httpRequest -> requestDispatcher.serve(httpRequest, appRegistry);
        this.start();
        LOGGER.info("Carbon UI Server activated.");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        this.bundleContext = null;
        this.stop();
        LOGGER.info("Carbon UI Server deactivated.");
    }

    private void start() {
        Map<String, String> availableApps = appRegistry.getAvailableApps();
        LOGGER.debug("'" + availableApps.size() + "' web app(s) found.");
        httpConnector.registerApps(availableApps, httpListener);
    }

    private void stop() {
        // TODO: 9/4/17 to be implemented
    }

    @Override
    public Extension getExtensionsOfApp(String appName, String extensionType) {
        return null;
    }
}
