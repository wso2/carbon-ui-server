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

package org.wso2.carbon.uis.internal.http;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uis.api.http.HttpConnector;
import org.wso2.carbon.uis.api.http.HttpRequest;
import org.wso2.carbon.uis.api.http.HttpResponse;
import org.wso2.msf4j.Microservice;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.function.Function;

/**
 * Created by sajith on 8/28/17.
 */
public class OsgiHttpConnector implements HttpConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsgiHttpConnector.class);

    private BundleContext bundleContext;

    public OsgiHttpConnector(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public void registerApp(String appName, String appContextPath, Function<HttpRequest, HttpResponse> httpListener) {
        Dictionary<String, String> dictionary = new Hashtable<>();
        dictionary.put("contextPath", appContextPath);
        bundleContext.registerService(Microservice.class, new WebappMicroservice(httpListener), dictionary);
        LOGGER.info("Webapp '{}' is available at '{}'.", appName, appContextPath);
    }
}
