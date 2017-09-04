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

import org.wso2.carbon.uis.api.Extension;

/**
 * Represents Carbon UI server.
 *
 * @since 0.8.0
 */
public interface Server {

    /**
     * Returns extensions of given app.
     *
     * @param appName       name of the app
     * @param extensionType type of extensions
     * @return specified type of extensions of the specified app
     */
    Extension getExtensionsOfApp(String appName, String extensionType);
}
