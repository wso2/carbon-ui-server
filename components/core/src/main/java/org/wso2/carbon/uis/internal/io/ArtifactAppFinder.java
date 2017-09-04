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

package org.wso2.carbon.uis.internal.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uis.internal.deployment.AppFinder;
import org.wso2.carbon.uis.internal.exception.DeploymentException;
import org.wso2.carbon.uis.internal.exception.FileOperationException;
import org.wso2.carbon.uis.internal.io.reference.ArtifactAppReference;
import org.wso2.carbon.uis.internal.reference.AppReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An app finder that locates apps from a directory.
 *
 * @since 1.0.0
 */
public class ArtifactAppFinder implements AppFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactAppFinder.class);

    private final Path appsRepository;
    private final Map<String, AppReference> availableApps;

    /**
     * Creates a new app finder that locates apps from {@code <CARBON_HOME>/deployment/reactapps} directory.
     */
    public ArtifactAppFinder() {
        this(Paths.get(System.getProperty("carbon.home", "."), "deployment", "reactapps"));
    }

    /**
     * Creates a new app finder that locates apps from the given directory.
     *
     * @param appsRepository app repository directory
     */
    public ArtifactAppFinder(Path appsRepository) {
        this.appsRepository = appsRepository;
        this.availableApps = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getAvailableApps() {
        Map<String, AppReference> foundApps = findApps(appsRepository);
        if (foundApps.isEmpty()) {
            throw new DeploymentException("No apps were found in '" + appsRepository + "'.");
        }
        Map<String, String> appNamesContextPaths = new HashMap<>();
        for (Map.Entry<String, AppReference> entry : foundApps.entrySet()) {
            AppReference appReference = entry.getValue();
            String appName = appReference.getName();
            String appContextPath = entry.getKey();
            availableApps.put(appContextPath, appReference);
            appNamesContextPaths.put(appName, appContextPath);
            LOGGER.debug("Web app '{}' found at '{}' for context path '{}'.", appName, appReference.getPath(),
                         appContextPath);
        }
        return appNamesContextPaths;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AppReference> getAppReference(String appContextPath) {
        return Optional.ofNullable(availableApps.get(appContextPath));
    }

    private Map<String, AppReference> findApps(Path appsRepository) {
        try {
            return Files.list(appsRepository)
                    .filter(Files::isDirectory)
                    .map(ArtifactAppReference::new)
                    .collect(Collectors.toMap(this::getAppContextPath, ar -> ar));
        } catch (IOException e) {
            throw new FileOperationException("Cannot list web apps in '" + appsRepository + "' directory.", e);
        }
    }

    private String getAppContextPath(AppReference appReference) {
        // TODO: 8/23/17 via deployment.yaml DevOps should be able to override app context path
        return "/" + appReference.getName();
    }
}
