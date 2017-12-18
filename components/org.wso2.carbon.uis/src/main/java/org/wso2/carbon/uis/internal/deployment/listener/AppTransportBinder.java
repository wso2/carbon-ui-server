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
import org.wso2.carbon.uis.internal.deployment.msf4j.WebappMicroservice;
import org.wso2.carbon.uis.internal.exception.AppDeploymentEventListenerException;
import org.wso2.carbon.uis.internal.http.RequestDispatcher;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Binder that registers web apps to the HTTP transport layer.
 * <p>
 * This class is responsible for binding a web app to the HTTP transport layer, when that web app gets deployed. When
 * the web app gets updeloyed, it will be unbound from the transport layer.
 *
 * @since 0.15.0
 */
public class AppTransportBinder implements AppDeploymentEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppTransportBinder.class);

    private final MicroservicesRegistrar microservicesRegistrar;
    private final ConcurrentMap<String, Set<MicroserviceRegistration>> microserviceRegistrations;
    private final ServerConfiguration serverConfiguration;

    /**
     * Creates a new app transport binder.
     *
     * @param microservicesRegistrar Microservices registrar
     * @param serverConfiguration    server configuration
     */
    public AppTransportBinder(MicroservicesRegistrar microservicesRegistrar,
                              ServerConfiguration serverConfiguration) {
        this.microservicesRegistrar = microservicesRegistrar;
        this.microserviceRegistrations = new ConcurrentHashMap<>();
        this.serverConfiguration = serverConfiguration;
    }

    @Override
    public void appDeploymentEvent(App app) throws AppDeploymentEventListenerException {
        String appName = app.getName();
        String appContextPath = app.getContextPath();
        WebappMicroservice microservice = createMicroservice(app);
        String transportId = serverConfiguration.getConfigurationForApp(appName)
                .flatMap(ServerConfiguration.AppConfiguration::getTransportId)
                .orElse(null);

        if (transportId == null) {
            Set<MicroserviceRegistration> registrations = microservicesRegistrar.register(microservice, appContextPath);
            if (registrations.isEmpty()) {
                throw new AppDeploymentEventListenerException(
                        "Cannot find any HTTPS transports to register web app '" + appName + "'.");
            } else {
                microserviceRegistrations.put(appName, registrations);
                registrations.stream()
                        .map(registration -> registration.getRegisteredHttpTransport().getUrlFor(appContextPath))
                        .forEach(appUrl -> LOGGER.info("Web app '{}' is available at '{}'.", appName, appUrl));
            }
        } else {
            MicroserviceRegistration registration;
            try {
                registration = microservicesRegistrar.register(microservice, appContextPath, transportId);
            } catch (IllegalArgumentException e) {
                throw new AppDeploymentEventListenerException(
                        "Cannot find a configured HTTP transport for ID '" + transportId + "' to register web app '" +
                        appName + "'.", e);
            }
            microserviceRegistrations.put(appName, Collections.singleton(registration));
            LOGGER.info("Web app '{}' is available at '{}'.", appName,
                        registration.getRegisteredHttpTransport().getUrlFor(appContextPath));
        }
    }

    @Override
    public void appUndeploymentEvent(String appName) throws AppDeploymentEventListenerException {
        Set<MicroserviceRegistration> microserviceRegistrations = this.microserviceRegistrations.remove(appName);
        if (microserviceRegistrations == null) {
            throw new AppDeploymentEventListenerException(
                    "Cannot unregister web app '" + appName + "'. App might be already unregistered or " +
                    "not be registered at all.");
        }

        microserviceRegistrations.forEach(microserviceRegistration -> {
            microserviceRegistration.unregister();
            LOGGER.debug("Web app '{}' unregistered from {}.", appName,
                         microserviceRegistration.getRegisteredHttpTransport());
        });
        LOGGER.info("Web app '{}' undeployed.", appName);
    }

    /**
     * Closes this binder.
     */
    public void close() {
        for (Set<MicroserviceRegistration> microserviceRegistration : this.microserviceRegistrations.values()) {
            microserviceRegistration.forEach(MicroserviceRegistration::unregister);
        }
        microserviceRegistrations.clear();
    }

    private static WebappMicroservice createMicroservice(App app) {
        RequestDispatcher requestDispatcher = new RequestDispatcher(app);
        return new WebappMicroservice((requestDispatcher::serve));
    }
}
