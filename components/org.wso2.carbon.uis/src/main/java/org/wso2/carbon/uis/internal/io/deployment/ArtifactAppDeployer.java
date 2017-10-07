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
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.uis.api.App;
import org.wso2.carbon.uis.api.exception.UISRuntimeException;
import org.wso2.carbon.uis.internal.deployment.AppCreator;
import org.wso2.carbon.uis.internal.deployment.AppDeploymentEventListener;
import org.wso2.carbon.uis.internal.exception.AppCreationException;
import org.wso2.carbon.uis.internal.exception.DeploymentException;
import org.wso2.carbon.uis.internal.io.reference.ArtifactAppReference;
import org.wso2.carbon.uis.internal.reference.AppReference;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An app deployer that finds web apps from a directory.
 *
 * @since 0.8.3
 */
@Component(name = "org.wso2.carbon.uis.internal.io.deployment.ArtifactAppDeployer",
           service = Deployer.class,
           immediate = true,
           property = {
                   "componentName=wso2-carbon-ui-server-deployer"
           }
)
public class ArtifactAppDeployer implements Deployer {

    private static final String ARTIFACT_TYPE = "web-ui-app";
    private static final String DEPLOYMENT_LOCATION = "file:web-ui-apps";
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactAppDeployer.class);

    private final ArtifactType<String> artifactType;
    private final URL deploymentLocation;
    private final ConcurrentMap<String, App> deployedApps;
    private AppDeploymentEventListener appDeploymentEventListener;

    /**
     * Creates a new app deployer.
     */
    public ArtifactAppDeployer() {
        this.artifactType = new ArtifactType<>(ARTIFACT_TYPE);
        URL deploymentLocationUrl = null;
        try {
            deploymentLocationUrl = new URL(DEPLOYMENT_LOCATION);
        } catch (MalformedURLException e) {
            LOGGER.error("Invalid URL '{}' as app deployment location.", DEPLOYMENT_LOCATION);
        }
        this.deploymentLocation = deploymentLocationUrl;
        this.deployedApps = new ConcurrentHashMap<>();
    }

    @Reference(name = "deploymentListener",
               service = AppDeploymentEventListener.class,
               cardinality = ReferenceCardinality.MANDATORY,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unregisterListener")
    protected void registerListener(AppDeploymentEventListener listener) {
        this.appDeploymentEventListener = listener;
        LOGGER.debug("An instance of class '{}' registered as an app deployment listener.",
                     listener.getClass().getName());
    }

    protected void unregisterListener(AppDeploymentEventListener listener) {
        this.appDeploymentEventListener = null;
        LOGGER.debug("An instance of class '{}' unregistered as an app deployment listener.",
                     listener.getClass().getName());
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        LOGGER.debug("Carbon UI server app deployer activated.");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        try {
            for (String artifactKey : deployedApps.keySet()) {
                undeploy(artifactKey);
            }
        } catch (CarbonDeploymentException e) {
            throw new UISRuntimeException("An error occurred during deactivation of app deployer.", e);
        }
        LOGGER.debug("Carbon UI server app deployer deactivated.");
    }

    @Override
    public void init() {
        LOGGER.debug("Carbon UI server app deployer initialized.");
    }

    @Override
    public Object deploy(Artifact artifact) throws CarbonDeploymentException {
        Path appPath = artifact.getFile().toPath();
        if (!isValidAppArtifact(appPath)) {
            throw new DeploymentException("Artifact located in '" + appPath + "'is not a valid web app.");
        }

        App app = createApp(appPath);
        String artifactKey = "webapp:" + app.getName();

        appDeploymentEventListener.appDeploymentEvent(app);
        deployedApps.put(artifactKey, app);
        LOGGER.debug("Web app '{}' deployed for context path '{}'.", app.getName(), app.getContextPath());
        return artifactKey;
    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        App app = deployedApps.remove(key);
        if (app == null) {
            throw new CarbonDeploymentException("Web app for key '{}' cannot be found to undeploy.");
        }

        appDeploymentEventListener.appUndeploymentEvent(app.getName());
        LOGGER.debug("Web app '{}' undeployed from context path '{}'.", app.getName(), app.getContextPath());
    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        // when an app artifact gets updated, we just re-deploy it
        undeploy(artifact.getKey());
        return deploy(artifact);
    }

    @Override
    public URL getLocation() {
        return deploymentLocation;
    }

    @Override
    public ArtifactType getArtifactType() {
        return artifactType;
    }

    private boolean isValidAppArtifact(Path appPath) throws CarbonDeploymentException {
        try {
            return Files.exists(appPath) && Files.isDirectory(appPath) && Files.isReadable(appPath) &&
                   !Files.isHidden(appPath);
        } catch (IOException e) {
            throw new CarbonDeploymentException("Cannot access web app artifact in '" + appPath + "'.", e);
        }
    }

    private App createApp(Path appPath) throws CarbonDeploymentException {
        AppReference appReference = new ArtifactAppReference(appPath);
        String appContextPath = createAppContextPath(appReference);
        try {
            return AppCreator.createApp(appReference, appContextPath);
        } catch (AppCreationException e) {
            throw new CarbonDeploymentException(
                    "Cannot create web app '" + appReference.getName() + "' from artifact '" + appReference.getPath() +
                    "' to deploy for context path '" + appContextPath + "'.", e);
        }
    }

    private String createAppContextPath(AppReference appReference) {
        return "/" + appReference.getName();
    }
}
