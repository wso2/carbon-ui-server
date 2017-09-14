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

package org.wso2.carbon.uis.internal.deployment;

import org.apache.commons.lang3.tuple.Pair;
import org.wso2.carbon.uis.api.App;
import org.wso2.carbon.uis.internal.exception.AppCreationException;
import org.wso2.carbon.uis.internal.reference.AppReference;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A registry that holds created or to-be-created web apps.
 *
 * @since 0.8.0
 */
public class AppRegistry {

    private final ConcurrentMap<String, App> createdApps = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Pair<AppReference, String>> pendingToCreateApps = new ConcurrentHashMap<>();

    /**
     * Adds a new web app to this registry.
     *
     * @param appReference reference to the app
     */
    public void addApp(AppReference appReference, String appContextPath) {
        pendingToCreateApps.put(appReference.getName(), Pair.of(appReference, appContextPath));
    }

    /**
     * Removes the specified web app from this registry.
     *
     * @param appName name of the app to be removed
     */
    public void removeApp(String appName) {
        if (createdApps.remove(appName) == null) {
            pendingToCreateApps.remove(appName);
        }
    }

    /**
     * Returns the created web app of the specified name. If the app is not created previously, then it will be created
     * and returned.
     *
     * @param appName name of the app
     * @return created app, or {@code null} if there is no app found for the given name
     * @throws AppCreationException if an error occurred during app creation
     */
    public App getApp(String appName) throws AppCreationException {
        return createdApps.computeIfAbsent(appName, this::createApp);
    }

    /**
     * Returns already created web apps in this registry.
     *
     * @return created apps
     */
    public Set<App> getCreatedApps() {
        return new HashSet<>(createdApps.values());
    }

    private App createApp(String appName) throws AppCreationException {
        Pair<AppReference, String> appReferenceContextPath = pendingToCreateApps.get(appName);
        if (appReferenceContextPath == null) {
            return null;
        }

        try {
            return AppCreator.createApp(appReferenceContextPath.getLeft(), appReferenceContextPath.getRight());
        } catch (Exception e) {
            throw new AppCreationException(
                    "Cannot create app '" + appReferenceContextPath.getLeft().getName() +
                    "' to deploy for context path '" + appReferenceContextPath.getRight() + "'.", e);
        }
    }
}
