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

package org.wso2.carbon.uis.api;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.Collections;
import java.util.Map;

/**
 * Bean class for server configurations.
 *
 * @since 0.12.6
 */
@Configuration(namespace = "wso2.carbon-ui-server", description = "Configurations for Carbon UI Server")
public class ServerConfiguration {

    @Element(description = "context paths for web apps")
    private Map<String, String> contextPaths = Collections.emptyMap();

    /**
     * Returns overrding context paths for web apps.
     *
     * @return overriding context paths
     */
    public Map<String, String> getContextPaths() {
        return contextPaths;
    }
}
