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
import org.wso2.carbon.uis.internal.reference.AppReference;

import java.util.List;
import java.util.Optional;

/**
 * A locator that finds UUF apps from an app repository.
 *
 * @since 1.0.0
 */
public interface AppFinder {

    /**
     * Returns names and context paths of available apps.
     * @return names and context paths of available apps
     */
    List<Pair<String, String>> getAvailableApps();

    /**
     * Returns an app reference to the app which is specified by the given context path.
     *
     * @param appContextPath app's context path
     * @return app reference for the given app
     */
    Optional<AppReference> getAppReference(String appContextPath);
}
