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

package org.wso2.carbon.uis.internal.deployment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uis.api.App;
import org.wso2.carbon.uis.internal.http.msf4j.MicroserviceRegistration;
import org.wso2.carbon.uis.internal.http.msf4j.MicroservicesRegistrar;
import org.wso2.carbon.uis.spi.RestApiProvider;
import org.wso2.msf4j.Microservice;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Deploys that registers REST APIs of web apps.
 * <p>
 * This class is responsible for registering REST APIs (Microservices) associated with a web app, when that web app gets
 * deployed. When the web app gets updeloyed, previously registered REST APIs will be unregistered.
 *
 * @since 0.15.0
 */
public class RestApiDeployer implements AppDeploymentEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestApiDeployer.class);

    private final MicroservicesRegistrar microservicesRegistrar;
    private final Map<String, RestApiProvider> restApiProviders;
    private final ConcurrentMap<String, Set<MicroserviceRegistration>> microserviceRegistrations;

    /**
     * Creates a new REST APIs deployer.
     *
     * @param restApiProviders       REST APIs providers
     * @param microservicesRegistrar Microservices registrar
     */
    public RestApiDeployer(Set<RestApiProvider> restApiProviders, MicroservicesRegistrar microservicesRegistrar) {
        this.restApiProviders = restApiProviders.stream()
                .collect(Collectors.toMap(RestApiProvider::getAppName, Function.identity()));
        this.microservicesRegistrar = microservicesRegistrar;
        this.microserviceRegistrations = new ConcurrentHashMap<>();
    }

    @Override
    public void appDeploymentEvent(App app) {
        RestApiProvider restApiProvider = restApiProviders.get(app.getName());
        if (restApiProvider == null) {
            return;
        }

        String appContextPath = app.getContextPath();
        boolean httpsOnly = app.getConfiguration().isHttpsOnly();
        Set<MicroserviceRegistration> registrations = restApiProvider.getMicroservices(app).entrySet().stream()
                .map(entry -> registerMicroservice((appContextPath + entry.getKey()), entry.getValue(), httpsOnly))
                .collect(Collectors.toSet());
        microserviceRegistrations.put(app.getName(), registrations);
    }

    @Override
    public void appUndeploymentEvent(String appName) {
        Set<MicroserviceRegistration> microserviceRegistrations = this.microserviceRegistrations.remove(appName);
        if (microserviceRegistrations != null) {
            microserviceRegistrations.forEach(MicroserviceRegistration::unregister);
            microserviceRegistrations.clear();
        }
    }

    /**
     * Closes this deployer.
     */
    public void close() {
        for (Set<MicroserviceRegistration> registrations : microserviceRegistrations.values()) {
            registrations.forEach(MicroserviceRegistration::unregister);
            registrations.clear();
        }
        microserviceRegistrations.clear();
        restApiProviders.clear();
    }

    private MicroserviceRegistration registerMicroservice(String contextPath, Microservice microservice,
                                                          boolean httpsOnly) {
        MicroserviceRegistration microserviceRegistration;
        if (httpsOnly) {
            microserviceRegistration = microservicesRegistrar.registerSecuredMicroservice(microservice, contextPath);
            LOGGER.debug("Microservice '{}' deployed as a HTTPS REST API in context path '{}'.",
                         microservice, contextPath);
        } else {
            microserviceRegistration = microservicesRegistrar.registerMicroservice(microservice, contextPath);
            LOGGER.debug("Microservice '{}' deployed as a HTTP REST API in context path '{}'.",
                         microservice, contextPath);
        }
        // TODO: 12/8/17 Print accessible URL for the deployed Microservice
        return microserviceRegistration;
    }
}
