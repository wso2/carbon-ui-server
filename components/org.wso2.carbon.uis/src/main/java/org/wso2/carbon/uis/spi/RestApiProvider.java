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

import org.wso2.msf4j.Microservice;

import java.util.Map;

/**
 * Provider that supplies microservices that should be deployed as REST APIs for a specified web app.
 *
 * @since 0.15.0
 */
public interface RestApiProvider {

    /**
     * Returns the name of the app that this provider supplies REST APIs.
     *
     * @return name of the app
     */
    String getAppName();

    /**
     * Returns microservices that needs to be deployed as REST APIs.
     * <p>
     * Key of the returning map is considered as the path (without the app context path) of the REST API.
     *
     * @return microservices to be deploy
     */
    Map<String, Microservice> getMicroservices();
}
