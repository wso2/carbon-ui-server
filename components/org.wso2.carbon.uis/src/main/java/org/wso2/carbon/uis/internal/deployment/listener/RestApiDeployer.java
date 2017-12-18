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

package org.wso2.carbon.uis.internal.deployment.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uis.api.App;
import org.wso2.carbon.uis.api.ServerConfiguration;
import org.wso2.carbon.uis.internal.deployment.AppDeploymentEventListener;
import org.wso2.carbon.uis.internal.deployment.msf4j.MicroserviceRegistration;
import org.wso2.carbon.uis.internal.deployment.msf4j.MicroservicesRegistrar;
import org.wso2.carbon.uis.internal.exception.AppDeploymentEventListenerException;
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
    private final ServerConfiguration serverConfiguration;
    private final ConcurrentMap<String, Set<MicroserviceRegistration>> microserviceRegistrations;

    /**
     * Creates a new REST APIs deployer.
     *
     * @param restApiProviders       REST APIs providers
     * @param microservicesRegistrar Microservices registrar
     * @param serverConfiguration    server configuration
     */
    public RestApiDeployer(Set<RestApiProvider> restApiProviders, MicroservicesRegistrar microservicesRegistrar,
                           ServerConfiguration serverConfiguration) {
        this.microservicesRegistrar = microservicesRegistrar;
        this.restApiProviders = restApiProviders.stream()
                .collect(Collectors.toMap(RestApiProvider::getAppName, Function.identity()));
        this.serverConfiguration = serverConfiguration;
        this.microserviceRegistrations = new ConcurrentHashMap<>();
    }

    @Override
    public void appDeploymentEvent(App app) throws AppDeploymentEventListenerException {
        RestApiProvider restApiProvider = restApiProviders.get(app.getName());
        if (restApiProvider == null) {
            LOGGER.debug("Web app '{}' does not have any REST APIs provider registered for it.", app.getName());
            return;
        }

        String appContextPath = app.getContextPath();
        String appName = app.getName();
        String transportId = serverConfiguration.getConfigurationForApp(appName)
                .flatMap(ServerConfiguration.AppConfiguration::getTransportId)
                .orElse(null);
        Map<String, Microservice> microservices = restApiProvider.getMicroservices(app);
        for (String contextPath : microservices.keySet()) {
            if ((contextPath == null) || contextPath.isEmpty() || (contextPath.charAt(0) != '/')) {
                throw new AppDeploymentEventListenerException(
                        "Invalid context path '" + contextPath + "' returned for a REST API by REST API Provider '" +
                        restApiProvider + "' for app '" + appName +
                        "'. Context path should be a nun-null, non-empty, and should start with a '/'.");
            }
        }
        Set<MicroserviceRegistration> microserviceRegistrations;

        if (transportId == null) {
            microserviceRegistrations = microservices.entrySet().stream()
                    .map(entry -> registerMicroservice(entry.getValue(), (appContextPath + entry.getKey())))
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());
        } else {
            microserviceRegistrations = microservices.entrySet().stream()
                    .map(entry -> registerMicroservice(entry.getValue(), appContextPath + entry.getKey(), transportId))
                    .collect(Collectors.toSet());
        }
        this.microserviceRegistrations.put(app.getName(), microserviceRegistrations);
    }

    @Override
    public void appUndeploymentEvent(String appName) throws AppDeploymentEventListenerException {
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

    private Set<MicroserviceRegistration> registerMicroservice(Microservice microservice, String contextPath) {
        Set<MicroserviceRegistration> registrations = microservicesRegistrar.register(microservice, contextPath);
        if (registrations.isEmpty()) {
            throw new AppDeploymentEventListenerException(
                    "Cannot find any HTTPS transports to register Microservice " + microservice +
                    " as a REST API to context path '" + contextPath + "'.");
        } else {
            for (MicroserviceRegistration registration : registrations) {
                LOGGER.debug("Microservice '{}' is available as a HTTPS REST API at '{}'.", microservice,
                             registration.getRegisteredHttpTransport().getUrlFor(contextPath));
            }
        }
        return registrations;
    }

    private MicroserviceRegistration registerMicroservice(Microservice microservice, String contextPath,
                                                          String transportId) {
        MicroserviceRegistration microserviceRegistration;
        try {
            microserviceRegistration = microservicesRegistrar.register(microservice, contextPath, transportId);
        } catch (IllegalArgumentException e) {
            throw new AppDeploymentEventListenerException(
                    "Cannot find a configured HTTP transport for ID '" + transportId + "' to deploy REST APIs.");
        }

        LOGGER.debug("Microservice '{}' is available as a HTTP REST API at '{}'.", microservice,
                     microserviceRegistration.getRegisteredHttpTransport().getUrlFor(contextPath));
        return microserviceRegistration;
    }
}
