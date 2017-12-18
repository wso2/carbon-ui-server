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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.uis.api.App;
import org.wso2.carbon.uis.api.ServerConfiguration;
import org.wso2.carbon.uis.internal.deployment.AppCreator;
import org.wso2.carbon.uis.internal.deployment.AppDeploymentEventListener;
import org.wso2.carbon.uis.internal.deployment.AppRegistry;
import org.wso2.carbon.uis.internal.exception.AppCreationException;
import org.wso2.carbon.uis.internal.impl.OverriddenApp;
import org.wso2.carbon.uis.internal.io.reference.ArtifactAppReference;
import org.wso2.carbon.uis.internal.reference.AppReference;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * A web app deployer for Carbon Deployment engine.
 *
 * @since 0.8.3
 */
public class ArtifactAppDeployer implements Deployer {

    private static final String ARTIFACT_TYPE = "web-ui-app";
    private static final String DEPLOYMENT_LOCATION = "file:web-ui-apps";
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactAppDeployer.class);

    private final ArtifactType<String> artifactType;
    private final URL deploymentLocation;
    private final AppRegistry appRegistry;
    private final List<AppDeploymentEventListener> appDeploymentEventListeners;
    private final ServerConfiguration serverConfiguration;

    /**
     * Creates a new app deployer.
     *
     * @param appDeploymentEventListeners app deployment event listeners
     * @param serverConfiguration         server configurations
     */
    public ArtifactAppDeployer(List<AppDeploymentEventListener> appDeploymentEventListeners,
                               ServerConfiguration serverConfiguration) {
        this.appDeploymentEventListeners = appDeploymentEventListeners;
        this.serverConfiguration = serverConfiguration;
        this.artifactType = new ArtifactType<>(ARTIFACT_TYPE);
        this.deploymentLocation = getLocationUrl();
        this.appRegistry = new AppRegistry();
    }

    @Override
    public void init() {
        LOGGER.debug("Carbon UI server app deployer initialized.");
    }

    @Override
    public Object deploy(Artifact artifact) throws CarbonDeploymentException {
        Path appPath = artifact.getFile().toPath();
        if (!isValidAppArtifact(appPath)) {
            throw new CarbonDeploymentException("Artifact located in '" + appPath + "'is not a valid web app.");
        }

        App createdApp = createApp(appPath);
        App deployingApp = appRegistry.find(createdApp::canOverrideBy)
                .map(previouslyCreatedApp -> {
                    LOGGER.info("Undeploying {} in order to merge it with {} and re-deploy the merged web app.",
                                previouslyCreatedApp, createdApp);
                    publishAppUndeploymentEvent(previouslyCreatedApp);
                    appRegistry.add(createdApp);
                    return (App) new OverriddenApp(createdApp, previouslyCreatedApp);
                })
                .orElse(createdApp);

        publishAppDeploymentEvent(deployingApp);
        return appRegistry.add(deployingApp);
    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        Optional<App> removingApp = appRegistry.remove(key.toString());
        if (removingApp.isPresent()) {
            Optional<App> overriddenApp = appRegistry.find(app -> app.hasOverriddenBy(removingApp.get()));

            if (overriddenApp.isPresent()) {
                LOGGER.info("{} was overridden by the just undeployed {}. " +
                            "Therefore it will be undeployed and base {} will be restored.",
                            overriddenApp.get(), removingApp.get(), overriddenApp.get().getBase());
                appRegistry.remove(overriddenApp.get());
                publishAppUndeploymentEvent(overriddenApp.get());
                publishAppDeploymentEvent(overriddenApp.get().getBase());
            } else {
                publishAppUndeploymentEvent(removingApp.get());
            }
        } else {
            LOGGER.warn("Cannot find a deployed app for artifact key '{}'.", key);
        }
    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        LOGGER.debug("Ignored update of web app artifact at '{}'.", artifact.getPath());
        return artifact.getKey();
    }

    @Override
    public URL getLocation() {
        return deploymentLocation;
    }

    @Override
    public ArtifactType getArtifactType() {
        return artifactType;
    }

    /**
     * Closes this deployer.
     */
    public void close() {
        appRegistry.clear();
    }

    private void publishAppDeploymentEvent(App app) {
        appDeploymentEventListeners.forEach(listener -> listener.appDeploymentEvent(app));
        LOGGER.debug("Web app '{}' in '{}' deployed successfully.", app.getName(), app.getHighestPriorityPath());
    }

    private void publishAppUndeploymentEvent(App app) {
        appDeploymentEventListeners.forEach(listner -> listner.appUndeploymentEvent(app.getName()));
        LOGGER.debug("Web app '{}' in '{}' undeployed successfully.", app.getName(), app.getHighestPriorityPath());
    }

    private App createApp(Path appPath) throws CarbonDeploymentException {
        AppReference appReference = new ArtifactAppReference(appPath);
        String appContextPath = getAppContextPath(appReference);
        try {
            return AppCreator.createApp(appReference, appContextPath);
        } catch (AppCreationException e) {
            throw new CarbonDeploymentException(
                    "Cannot create web app '" + appReference.getName() + "' from artifact '" + appReference.getPath() +
                    "' to deploy for context path '" + appContextPath + "'.", e);
        }
    }

    private String getAppContextPath(AppReference appReference) throws CarbonDeploymentException {
        String appName = appReference.getName();
        try {
            return serverConfiguration.getConfigurationForApp(appName)
                    .flatMap(ServerConfiguration.AppConfiguration::getContextPath)
                    .orElse("/" + appName); // default context path
        } catch (IllegalArgumentException e) {
            throw new CarbonDeploymentException(
                    "Cannot deploy web app '" + appName + "' as the configured app context path is invalid.", e);
        }
    }

    private static boolean isValidAppArtifact(Path appPath) throws CarbonDeploymentException {
        try {
            return Files.exists(appPath) && Files.isDirectory(appPath) && Files.isReadable(appPath) &&
                   !Files.isHidden(appPath);
        } catch (IOException e) {
            throw new CarbonDeploymentException("Cannot access web app artifact in '" + appPath + "'.", e);
        }
    }

    private static URL getLocationUrl() {
        try {
            return new URL(DEPLOYMENT_LOCATION);
        } catch (MalformedURLException e) {
            LOGGER.error("Invalid URL '{}' as app deployment location.", DEPLOYMENT_LOCATION, e);
            return null;
        }
    }
}
