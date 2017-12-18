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

import com.google.common.collect.ImmutableList;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.kernel.startupresolver.StartupServiceUtils;
import org.wso2.carbon.uis.api.ServerConfiguration;
import org.wso2.carbon.uis.internal.deployment.listener.AppTransportBinder;
import org.wso2.carbon.uis.internal.deployment.listener.RestApiDeployer;
import org.wso2.carbon.uis.internal.http.msf4j.MicroservicesRegistrar;
import org.wso2.carbon.uis.internal.io.deployment.ArtifactAppDeployer;
import org.wso2.carbon.uis.spi.RestApiProvider;
import org.wso2.carbon.uis.spi.Server;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.singletonMap;

/**
 * Startup lister service component.
 *
 * @since 0.15.0
 */
@Component(service = RequiredCapabilityListener.class,
           immediate = true,
           property = {
                   "componentName=" + StartupListener.CARBON_COMPONENT_NAME
           }
)
public class StartupListener implements RequiredCapabilityListener {

    protected static final String CARBON_COMPONENT_NAME = "carbon-ui-server-startup-listener";
    private static final Logger LOGGER = LoggerFactory.getLogger(StartupListener.class);

    private BundleContext bundleContext;
    private final Set<ServiceRegistration<?>> serviceRegistrations = new HashSet<>();

    private final Set<RestApiProvider> restApiProviders = ConcurrentHashMap.newKeySet();
    private ServerConfiguration serverConfiguration;
    private MicroservicesRegistrar microservicesRegistrar;

    private AppTransportBinder appTransportBinder;
    private RestApiDeployer restApiDeployer;
    private CarbonUiServer carbonUiServer;
    private ArtifactAppDeployer appDeployer;

    @Reference(service = RestApiProvider.class,
               cardinality = ReferenceCardinality.MULTIPLE,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetRestApiProvider")
    protected void setRestApiProvider(RestApiProvider restApiProvider) {
        this.restApiProviders.add(restApiProvider);
        StartupServiceUtils.updateServiceCache(CARBON_COMPONENT_NAME, RestApiProvider.class);
        LOGGER.debug("An instance of class '{}' registered as a REST APIs provider for '{}' web app.",
                     restApiProvider.getAppName(), restApiProvider.getClass().getName());
    }

    protected void unsetRestApiProvider(RestApiProvider restApiProvider) {
        this.restApiProviders.remove(restApiProvider);
        LOGGER.debug("An instance of class '{}' unregistered as a REST APIs provider for '{}' web app.",
                     restApiProvider.getAppName(), restApiProvider.getClass().getName());
    }

    @Reference(service = ConfigProvider.class,
               cardinality = ReferenceCardinality.MANDATORY,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetConfigProvider")
    protected void setConfigProvider(ConfigProvider configProvider) {
        try {
            this.serverConfiguration = configProvider.getConfigurationObject(ServerConfiguration.class);
        } catch (ConfigurationException e) {
            this.serverConfiguration = new ServerConfiguration();
            LOGGER.error("Cannot load server configurations from 'deployment.yaml'. Falling-back to defaults.", e);
        }
    }

    protected void unsetConfigProvider(ConfigProvider configProvider) {
        LOGGER.debug("An instance of class '{}' unregistered as a config provider.",
                     configProvider.getClass().getName());
    }

    @Reference(service = MicroservicesRegistrar.class,
               cardinality = ReferenceCardinality.MANDATORY,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetMicroservicesRegistrar")
    protected void setMicroservicesRegistrar(MicroservicesRegistrar microservicesRegistrar) {
        this.microservicesRegistrar = microservicesRegistrar;
    }

    protected void unsetMicroservicesRegistrar(MicroservicesRegistrar microservicesRegistrar) {
        this.microservicesRegistrar = null;
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        LOGGER.debug("{} activated.", this.getClass().getName());
    }

    @Deactivate
    protected void deactivate() {
        bundleContext = null;
        serviceRegistrations.forEach(ServiceRegistration::unregister);

        restApiProviders.clear();
        serverConfiguration = null;
        microservicesRegistrar = null;

        appTransportBinder.close();
        appTransportBinder = null;
        restApiDeployer.close();
        restApiDeployer = null;
        carbonUiServer.close();
        carbonUiServer = null;
        appDeployer.close();
        appDeployer = null;

        LOGGER.debug("{} deactivated.", this.getClass().getName());
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        appTransportBinder = new AppTransportBinder(microservicesRegistrar, serverConfiguration);
        restApiDeployer = new RestApiDeployer(restApiProviders, microservicesRegistrar, serverConfiguration);
        carbonUiServer = new CarbonUiServer();
        appDeployer = new ArtifactAppDeployer(ImmutableList.of(appTransportBinder, restApiDeployer, carbonUiServer),
                                              serverConfiguration);

        Dictionary<String, Object> properties = new Hashtable<>(singletonMap("skipCarbonStartupResolver", true));
        serviceRegistrations.add(bundleContext.registerService(Deployer.class, appDeployer, properties));
        LOGGER.debug("Web app deployer '{}' registered as a Carbon Deployer.", appDeployer.getClass().getName());
        serviceRegistrations.add(bundleContext.registerService(Server.class, carbonUiServer, properties));
        LOGGER.debug("Server '{}' registered as a Carbon UI Server.", carbonUiServer.getClass().getName());

        LOGGER.debug("Carbon UI Server Startup Listener fully activated.");
    }
}
