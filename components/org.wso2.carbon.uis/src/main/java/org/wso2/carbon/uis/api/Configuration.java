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
import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.uis.api.http.HttpResponse.HEADER_CACHE_CONTROL;
import static org.wso2.carbon.uis.api.http.HttpResponse.HEADER_EXPIRES;
import static org.wso2.carbon.uis.api.http.HttpResponse.HEADER_PRAGMA;
import static org.wso2.carbon.uis.api.http.HttpResponse.HEADER_X_CONTENT_TYPE_OPTIONS;
import static org.wso2.carbon.uis.api.http.HttpResponse.HEADER_X_FRAME_OPTIONS;
import static org.wso2.carbon.uis.api.http.HttpResponse.HEADER_X_XSS_PROTECTION;

import static java.util.Collections.emptyMap;

/**
 * Represents a configurations for a web App.
 *
 * @since 0.8.0
 */
public class Configuration {

    /**
     * Default configuration for an app.
     */
    public static final Configuration DEFAULT_CONFIGURATION;

    private final boolean httpsOnly;
    private final HttpResponseHeaders responseHeaders;

    static {
        DEFAULT_CONFIGURATION = new Configuration(false, new HttpResponseHeaders(emptyMap(), emptyMap()));
    }

    /**
     * Creates a new configuration.
     *
     * @param httpsOnly       whether the associated web app is HTTPS only or not.
     * @param responseHeaders HTTP response headers configuration
     */
    public Configuration(boolean httpsOnly, HttpResponseHeaders responseHeaders) {
        this.httpsOnly = httpsOnly;
        this.responseHeaders = responseHeaders;
    }

    /**
     * Returns whether the associated web app is HTTPS only or not.
     *
     * @return {@code true} if the associated web app is HTTPS only, otherwise {@code false}
     */
    public boolean isHttpsOnly() {
        return httpsOnly;
    }

    /**
     * Returns HTTP response headers configuration in this app configuration.
     *
     * @return HTTP response headers configuration
     */
    public HttpResponseHeaders getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Represents a HTTP response headers configuration.
     *
     * @since 0.8.0
     */
    public static class HttpResponseHeaders {

        private final Map<String, String> pages;
        private final Map<String, String> staticResources;

        /**
         * Creates a new configuration.
         *
         * @param pages           HTTP response headers for pages
         * @param staticResources HTTP response headers for static resources
         */
        public HttpResponseHeaders(Map<String, String> pages, Map<String, String> staticResources) {
            Map<String, String> pagesHttpHeaders = new HashMap<>();
            pagesHttpHeaders.put(HEADER_X_CONTENT_TYPE_OPTIONS, "nosniff");
            pagesHttpHeaders.put(HEADER_X_XSS_PROTECTION, "1; mode=block");
            pagesHttpHeaders.put(HEADER_CACHE_CONTROL, "no-store, no-cache, must-revalidate, private");
            pagesHttpHeaders.put(HEADER_EXPIRES, "0");
            pagesHttpHeaders.put(HEADER_PRAGMA, "no-cache");
            pagesHttpHeaders.put(HEADER_X_FRAME_OPTIONS, "DENY");
            pagesHttpHeaders.putAll(pages);
            this.pages = Collections.unmodifiableMap(pagesHttpHeaders);

            Map<String, String> staticResourcesHttpHeaders = new HashMap<>();
            staticResourcesHttpHeaders.put(HEADER_CACHE_CONTROL, "public,max-age=2592000");
            staticResourcesHttpHeaders.putAll(staticResources);
            this.staticResources = Collections.unmodifiableMap(staticResourcesHttpHeaders);
        }

        /**
         * Returns HTTP response headers for pages.
         *
         * @return HTTP response headers for pages
         */
        public Map<String, String> forPages() {
            return pages;
        }

        /**
         * Returns HTTP response headers for static resources.
         *
         * @return HTTP response headers for static resources
         */
        public Map<String, String> forStaticResources() {
            return staticResources;
        }
    }
}
