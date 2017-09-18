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

package org.wso2.carbon.uis.internal.io.deployment;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uis.internal.deployment.AppDeployer;
import org.wso2.carbon.uis.internal.deployment.AppDeploymentEventListener;
import org.wso2.carbon.uis.internal.exception.DeploymentException;
import org.wso2.carbon.uis.internal.io.reference.ArtifactAppReference;
import org.wso2.carbon.uis.internal.reference.AppReference;
import org.wso2.carbon.uis.spi.Server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An app deployer that finds web apps from a directory.
 *
 * @since 0.8.3
 */
@Component(name = "org.wso2.carbon.uis.internal.io.deployment.ArtifactAppDeployer",
           service = Server.class,
           immediate = true,
           property = {
                   "componentName=wso2-carbon-ui-server-deployer"
           }
)
@SuppressWarnings("unused")
public class ArtifactAppDeployer implements AppDeployer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactAppDeployer.class);

    private final Path appsRepository;
    private final Set<AppDeploymentEventListener> eventListeners = new HashSet<>();
    private final Set<String> deployedAppNames = new HashSet<>();

    /**
     * Creates a new app deployer that locates apps from {@code <CARBON_HOME>/deployment/reactapps} directory.
     */
    public ArtifactAppDeployer() {
        this(Paths.get(System.getProperty("carbon.home", "."), "deployment", "reactapps"));
    }

    /**
     * Creates a new app deployer that locates apps from the given directory.
     *
     * @param appsRepository app repository directory
     */
    public ArtifactAppDeployer(Path appsRepository) {
        this.appsRepository = appsRepository;
    }

    @Reference(name = "deploymentListener",
               service = AppDeploymentEventListener.class,
               cardinality = ReferenceCardinality.AT_LEAST_ONE,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unregisterListener")
    protected void registerListener(AppDeploymentEventListener listener) {
        this.eventListeners.add(listener);
        LOGGER.debug("An instance of class '{}' registered as an app deployment listener.",
                     listener.getClass().getName());
    }

    protected void unregisterListener(AppDeploymentEventListener listener) {
        this.eventListeners.remove(listener);
        LOGGER.debug("An instance of class '{}' unregistered as an app deployment listener.",
                     listener.getClass().getName());
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        start();
        LOGGER.debug("Carbon UI server app deployer activated.");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        stop();
        LOGGER.debug("Carbon UI server app deployer deactivated.");
    }

    private void start() {
        if (!Files.exists(appsRepository)) {
            LOGGER.debug("Web apps repository '{}' does not exists.", appsRepository.toString());
            return;
        }

        Set<AppReference> appReferences;
        try {
            appReferences = Files.list(appsRepository)
                    .filter(Files::isDirectory)
                    .map(ArtifactAppReference::new)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new DeploymentException("Cannot list web apps in '" + appsRepository + "' directory.", e);
        }
        publishAppsDeploymentEvents(appReferences);
    }

    private void stop() {
        for (String appName : deployedAppNames) {
            publishAppUndeploymentEvent(appName);
        }
    }

    private void publishAppDeploymentEvent(AppReference deployingAppReference) {
        for (AppDeploymentEventListener appDeploymentEventListener : eventListeners) {
            appDeploymentEventListener.appDeploymentEvent(deployingAppReference);
        }
        deployedAppNames.add(deployingAppReference.getName());
    }

    private void publishAppsDeploymentEvents(Set<AppReference> deployingAppsReferences) {
        eventListeners.forEach(listener -> listener.appsDeploymentEvents(deployingAppsReferences));
    }

    private void publishAppUndeploymentEvent(String undeployingAppName) {
        for (AppDeploymentEventListener appDeploymentEventListener : eventListeners) {
            appDeploymentEventListener.appUndeploymentEvent(undeployingAppName);
        }
        deployedAppNames.remove(undeployingAppName);
    }
}
