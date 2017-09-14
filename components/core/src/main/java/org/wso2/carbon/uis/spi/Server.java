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

package org.wso2.carbon.uis.spi;

import org.wso2.carbon.uis.api.App;

import java.util.Optional;

/**
 * Represents Carbon UI server.
 *
 * @since 0.8.0
 */
public interface Server {

    /**
     * Returns fully deployed web app with the specified name.
     *
     * @param appName name of the app
     * @return app with the specified name; {@link Optional#empty() empty} if there is no app with the given name or app
     * is not deployed yet
     */
    Optional<App> getApp(String appName);
}
