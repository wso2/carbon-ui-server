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

package org.wso2.carbon.uis.api.http;

import java.util.function.Function;

/**
 * HTTP connector bridges the HTTP transport to the web app server.
 *
 * @since 0.8.0
 */
public interface HttpConnector {

    /**
     * Registers a web app to the transport.
     *
     * @param appName        name of the app
     * @param appContextPath context path of the app
     * @param httpListener   HTTP requests listener that handles the incoming HTTP requests for the registering app
     */
    void registerApp(String appName, String appContextPath, Function<HttpRequest, HttpResponse> httpListener);

    /**
     * Unregisters the specified app from the transport.
     *
     * @param appName name of the app to be unregister
     */
    void unregisterApp(String appName);

    /**
     * Unregisters all registered apps from the transport.
     */
    void unregisterAllApps();
}
