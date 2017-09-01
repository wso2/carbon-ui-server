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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the final configuration of a web App.
 *
 * @since 0.8.0
 */
public class Configuration {

    private String contextPath;
    private ResponseHeaders responseHeaders = ResponseHeaders.emptyResponseHeaders();

    public Configuration() {
    }

    /**
     * Returns the configured client-side context path for the app.
     *
     * @return client-side context path
     */
    public Optional<String> getContextPath() {
        return Optional.ofNullable(contextPath);
    }

    /**
     * Sets the client-side context path for the app.
     *
     * @param contextPath client-side context path to be set
     * @throws IllegalArgumentException if the context path is empty or doesn't start with a '/'
     * @see #getContextPath()
     */
    public void setContextPath(String contextPath) {
        if (contextPath != null) {
            if (contextPath.isEmpty()) {
                throw new IllegalArgumentException("Context path cannot be empty.");
            } else if (contextPath.charAt(0) != '/') {
                throw new IllegalArgumentException("Context path must start with a '/'. Instead found '" +
                                                   contextPath.charAt(0) + "' at the beginning.");
            }
        }
        this.contextPath = contextPath;
    }

    /**
     * Returns the configured HTTP headers for the response in the security configuration.
     *
     * @return HTTP headers for the response
     */
    public ResponseHeaders getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Sets the HTTP headers for the response in the security configuration.
     *
     * @param responseHeaders HTTP headers for the response
     */
    public void setResponseHeaders(ResponseHeaders responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    /**
     * Bean class that represents security headers configurations in the app's config file of an UUF App.
     *
     * @since 1.0.0
     */
    public static class ResponseHeaders {

        private Map<String, String> staticResources = Collections.emptyMap();
        private Map<String, String> pages = Collections.emptyMap();

        public ResponseHeaders(Map<String, String> staticResources, Map<String, String> pages) {
            this.staticResources = staticResources;
            this.pages = pages;
        }

        /**
         * Returns HTTP response headers for static contents.
         *
         * @return HTTP response headers
         */
        public Map<String, String> getStaticResources() {
            return staticResources;
        }

        /**
         * Sets the HTTP response headers for static contents.
         *
         * @param staticResources HTTP response headers to be set
         */
        public void setStaticResources(Map<String, String> staticResources) {
            this.staticResources = staticResources;
        }

        /**
         * Returns HTTP response headers for pages.
         *
         * @return HTTP response headers
         */
        public Map<String, String> getPages() {
            return pages;
        }

        /**
         * Sets the HTTP response headers for pages.
         *
         * @param pages HTTP response headers to be set
         */
        public void setPages(Map<String, String> pages) {
            this.pages = pages;
        }

        /**
         * Creates a new empty response headers.
         * @return empty response headers
         */
        static ResponseHeaders emptyResponseHeaders() {
            return new ResponseHeaders(Collections.emptyMap(), Collections.emptyMap());
        }
    }
}
