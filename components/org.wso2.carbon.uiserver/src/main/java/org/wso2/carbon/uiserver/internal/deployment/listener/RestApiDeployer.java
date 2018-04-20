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

package org.wso2.carbon.uiserver.internal.deployment.listener;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uiserver.api.App;
import org.wso2.carbon.uiserver.api.ServerConfiguration;
import org.wso2.carbon.uiserver.internal.deployment.AppDeploymentEventListener;
import org.wso2.carbon.uiserver.internal.deployment.msf4j.MicroserviceRegistration;
import org.wso2.carbon.uiserver.internal.deployment.msf4j.MicroservicesRegistrar;
import org.wso2.carbon.uiserver.internal.exception.AppDeploymentEventListenerException;
import org.wso2.carbon.uiserver.spi.RestApiProvider;
import org.wso2.msf4j.Microservice;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.collect.Multimaps.toMultimap;

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
    private final SetMultimap<String, RestApiProvider> restApiProviders;
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
                .collect(toMultimap(RestApiProvider::getAppName, Function.identity(), HashMultimap::create));
        this.serverConfiguration = serverConfiguration;
        this.microserviceRegistrations = new ConcurrentHashMap<>();
    }

    @Override
    public void appDeploymentEvent(App app) throws AppDeploymentEventListenerException {
        String appName = app.getName();
        Set<RestApiProvider> restApiProviders = this.restApiProviders.get(appName);
        if (restApiProviders == null) {
            LOGGER.debug("Web app '{}' does not have any REST APIs provider registered for it.", appName);
            return;
        }

        Map<String, Microservice> microservices = collectMicroservices(restApiProviders, app);
        String transportId = serverConfiguration.getConfigurationForApp(appName).flatMap(
                ServerConfiguration.AppConfiguration::getTransportId).orElse(null);
        Set<MicroserviceRegistration> microserviceRegistrations;
        if (transportId == null) {
            microserviceRegistrations = microservices.entrySet().stream()
                    .map(entry -> registerMicroservice(entry.getValue(), entry.getKey()))
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());
        } else {
            microserviceRegistrations = microservices.entrySet().stream()
                    .map(entry -> registerMicroservice(entry.getValue(), entry.getKey(), transportId))
                    .collect(Collectors.toSet());
        }
        this.microserviceRegistrations.put(appName, microserviceRegistrations);
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

    private Map<String, Microservice> collectMicroservices(Set<RestApiProvider> restApiProviders, App app) {
        Map<String, Microservice> allMicroservices = new HashMap<>();
        for (RestApiProvider restApiProvider : restApiProviders) {
            Map<String, Microservice> microservices = restApiProvider.getMicroservices(app);
            for (Map.Entry<String, Microservice> entry : microservices.entrySet()) {
                String contextPath = entry.getKey();
                if ((contextPath == null) || contextPath.isEmpty() || (contextPath.charAt(0) != '/')) {
                    throw new AppDeploymentEventListenerException(
                            "Invalid context path '" + contextPath + "'returned for a REST API by REST API Provider '" +
                            restApiProvider + "' for app '" + app.getName() +
                            "'. Context path should be a nun-null, non-empty, and should start with a '/'.");
                }
                Microservice microservice = entry.getValue();
                if (microservice == null) {
                    throw new AppDeploymentEventListenerException(
                            "Cannot register null Microservice to context path '" + contextPath + "' for app '" +
                            app.getName() + "'.");
                }
                allMicroservices.put((app.getContextPath() + contextPath), microservice);
            }
        }
        return allMicroservices;
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
