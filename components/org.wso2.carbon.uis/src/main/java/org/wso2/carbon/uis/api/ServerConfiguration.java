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
import java.util.Optional;

/**
 * Bean class for server configurations.
 *
 * @since 0.12.6
 */
@Configuration(namespace = "wso2.carbon-ui-server", description = "Configurations for Carbon UI Server")
public class ServerConfiguration {

    @Element(description = "Configurations for web apps.\n" +
                           "Here key is the name of the web app abd value is configurations for that web app.")
    private Map<String, AppConfiguration> apps = Collections.emptyMap();

    /**
     * Returns configurations for the specified app.
     *
     * @param appName name of the app
     * @return server configurations of the app
     */
    public Optional<AppConfiguration> getConfigurationForApp(String appName) {
        return Optional.ofNullable(apps.get(appName));
    }

    /**
     * Bean class for configurations of a web app.
     *
     * @since 0.18.0
     */
    public static class AppConfiguration {

        @Element(description = "Context path of this web app.\n" +
                               "This overrides the default context path (which is '/'+<app-name>) of the app. " +
                               "Context path should start with a '/' (e.g. '/foo').")
        private String contextPath;

        @Element(description = "ID of the HTTP listener configuration that this web app should be deployed.\n" +
                               "'listenerConfigurations' can be found under 'wso2.transport.http' namespace. " +
                               "If absent, this web app will be deployed to all available HTTPS transports.")
        private String transportId;

        /**
         * Returns the context path in this app configuration.
         *
         * @return the context path
         * @throws IllegalArgumentException if configured context path is invalid
         */
        public Optional<String> getContextPath() throws IllegalArgumentException {
            if ((contextPath != null) && (contextPath.charAt(0) != '/')) {
                throw new IllegalArgumentException(
                        "Configured context path '" + contextPath + "' is invalid as it does not start with a '/'.");
            }
            return Optional.ofNullable(contextPath);
        }

        /**
         * Returns the transport ID in this app configuration.
         *
         * @return the transport ID
         * @throws IllegalArgumentException if configured transport ID is invalid
         */
        public Optional<String> getTransportId() throws IllegalArgumentException {
            if ((transportId != null) && transportId.isEmpty()) {
                throw new IllegalArgumentException("Configured transport ID is invalid as it cannot be a empty.");
            }
            return Optional.ofNullable(transportId);
        }
    }
}
