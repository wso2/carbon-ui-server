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
import org.wso2.carbon.kernel.startupresolver.CapabilityProvider;
import org.wso2.carbon.uis.api.Extension;
import org.wso2.carbon.uis.api.http.HttpConnector;
import org.wso2.carbon.uis.internal.deployment.AppDeploymentEventListener;
import org.wso2.carbon.uis.internal.deployment.AppRegistry;
import org.wso2.carbon.uis.internal.reference.AppReference;
import org.wso2.carbon.uis.spi.Server;
import org.wso2.msf4j.Microservice;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;

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
public class CarbonUiServer implements Server, AppDeploymentEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarbonUiServer.class);

    private final AppRegistry appRegistry = new AppRegistry();
    private final RequestDispatcher requestDispatcher = new RequestDispatcher();
    private HttpConnector httpConnector;
    private BundleContext bundleContext;

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
        bundleContext.registerService(AppDeploymentEventListener.class, this, null);
        LOGGER.debug("Carbon UI Server activated.");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        this.bundleContext = null;
        httpConnector.unregisterAllApps();
        LOGGER.debug("Carbon UI Server deactivated.");
    }

    @Override
    public Extension getExtensionsOfApp(String appName, String extensionType) {
        throw new UnsupportedOperationException("to be implemented");
    }

    @Override
    public void appDeploymentEvent(AppReference appReference) {
        String appName = appReference.getName();
        String appContextPath = getAppContextPath(appReference);
        appRegistry.addApp(appReference, appContextPath);
        httpConnector.registerApp(appName, appContextPath,
                                  request -> requestDispatcher.serve(request, appRegistry, appName));
    }

    @Override
    public void appsDeploymentEvents(Set<AppReference> appReferences) {
        appReferences.forEach(this::appDeploymentEvent);

        // Register CapabilityProvider to give number of Microservice capabilities provided by this bundle.
        Dictionary<String, String> properties = new Hashtable<>();
        properties.put("capabilityName", Microservice.class.getName());
        bundleContext.registerService(CapabilityProvider.class, () -> appReferences.size() * 2, properties);
    }

    @Override
    public void appUndeploymentEvent(String appName) {
        appRegistry.removeApp(appName);
        httpConnector.unregisterApp(appName);
    }

    private String getAppContextPath(AppReference appReference) {
        return "/" + appReference.getName();
    }
}
