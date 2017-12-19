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

package org.wso2.carbon.uiserver.internal.deployment;

import org.wso2.carbon.uiserver.api.App;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Registry that holds created web apps.
 *
 * @since 0.12.0
 */
public class AppRegistry {

    private final Map<String, App> apps = new HashMap<>();

    /**
     * Adds an app to this registry.
     *
     * @param app app to be added
     * @return a key that can be used later to retrieve the added app object
     * @see #get(String)
     */
    public String add(App app) {
        String key = keyFor(app);
        apps.put(key, app);
        return key;
    }

    /**
     * Returns the app in this registry associated for the supplied key.
     *
     * @param key key whose associated app to be returned
     * @return associated app, or {@link Optional#empty() empty} if there is no app for the key
     * @see #add(App)
     */
    public Optional<App> get(String key) {
        return Optional.ofNullable(apps.get(key));
    }

    /**
     * Returns all apps added to this registry.
     *
     * @return all apps added to the registry
     */
    public Collection<App> getAll() {
        return apps.values();
    }

    /**
     * Removes & returns the app in this registry associated for the supplied key.
     *
     * @param key key whose associated app to be removed & returned
     * @return associated app, or {@link Optional#empty() empty} if there is no app for the key
     * @see #remove(App)
     */
    public Optional<App> remove(String key) {
        return Optional.ofNullable(apps.remove(key));
    }

    /**
     * Removes the specified app from this register. Nothing changes if the specified app doesn't exist in the
     * registry.
     *
     * @return {@code true} if the app was found & removed, otherwise {@code false}
     */
    public boolean remove(App app) {
        return apps.remove(keyFor(app)) != null;
    }

    /**
     * Returns the very first app in this registry that satisfies the specified perdicate.
     *
     * @param predicate a non-interfering, stateless predicate to apply to app to determine if it should be chosen
     * @return very first app that satisfies the predicate, if non found then {@link Optional#empty()}
     */
    public Optional<App> find(Predicate<App> predicate) {
        return apps.values().stream().filter(predicate).findFirst();
    }

    /**
     * Removes all apps in this registry.
     */
    public void clear() {
        apps.clear();
    }

    private static String keyFor(App app) {
        return app.getName() + app.getPaths();
    }
}
