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

package org.wso2.carbon.uis.internal.deployment.parser;

import org.wso2.carbon.uis.api.Configuration;

import java.util.Map;

/**
 * Bean class that represents the {@code configuration.yaml} file of a web app.
 *
 * @since 0.12.1
 */
public class ConfigurationYaml {

    private ResponseHeaders responseHeaders;

    /**
     * Returns {@code responseHeaders} config value.
     *
     * @return {@code responseHeaders} config.
     */
    public ResponseHeaders getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Sets {@code responseHeaders} config value.
     *
     * @param responseHeaders config value
     */
    public void setResponseHeaders(ResponseHeaders responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    /**
     * Returns this YAML configuration as a {@link Configuration} object.
     *
     * @return {@link Configuration} object
     */
    public Configuration toConfiguration() {
        return new Configuration(new Configuration.HttpResponseHeaders(this.responseHeaders.getPages(),
                                                                       this.responseHeaders.getResources()));
    }

    /**
     * Bean class for {@code responseHeaders} config.
     *
     * @since 0.12.1
     */
    public static class ResponseHeaders {

        private Map<String, String> pages;
        private Map<String, String> resources;

        /**
         * Returns {@code pages} config value.
         *
         * @return {@code pages} config value.
         */
        public Map<String, String> getPages() {
            return pages;
        }

        /**
         * Sets {@code pages} config value.
         *
         * @param pages config value.
         */
        public void setPages(Map<String, String> pages) {
            this.pages = pages;
        }

        /**
         * Returns {@code resources} config value.
         *
         * @return {@code resources} config value
         */
        public Map<String, String> getResources() {
            return resources;
        }

        /**
         * Sets {@code resources} config value.
         *
         * @param resources config value
         */
        public void setResources(Map<String, String> resources) {
            this.resources = resources;
        }
    }
}
