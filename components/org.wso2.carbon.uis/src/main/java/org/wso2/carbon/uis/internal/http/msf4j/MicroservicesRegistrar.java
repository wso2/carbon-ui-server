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

package org.wso2.carbon.uis.internal.http.msf4j;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uis.internal.http.HttpTransport;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.MicroservicesServer;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Registrar that registers Microservices to OSGi environment.
 *
 * @since 0.15.0
 */
@Component(service = MicroservicesRegistrar.class,
           immediate = true)
public class MicroservicesRegistrar {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicroservicesRegistrar.class);

    private final Set<HttpTransport> httpTransports = new HashSet<>();
    private BundleContext bundleContext;

    @Activate
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        LOGGER.debug("Microservices registrar activated.");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        this.bundleContext = null;
        LOGGER.debug("Microservices registrar deactivated.");
    }

    @Reference(service = MicroservicesServer.class,
               cardinality = ReferenceCardinality.AT_LEAST_ONE,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetMicroservicesServer")
    protected void setMicroservicesServer(MicroservicesServer microservicesServer) {
        microservicesServer.getListenerConfigurations()
                .forEach((listenerInterfaceId, listenerConfiguration) -> {
                    HttpTransport httpTransport = new HttpTransport(listenerInterfaceId, listenerConfiguration.getId(),
                                                                    listenerConfiguration.getScheme(),
                                                                    listenerConfiguration.getHost(),
                                                                    listenerConfiguration.getPort());
                    httpTransports.add(httpTransport);
                    LOGGER.debug("HTTP transport {} is available.", httpTransport);
                });
        LOGGER.debug("Microservices Server '{}' registered.", microservicesServer.getClass().getName());
    }

    protected void unsetMicroservicesServer(MicroservicesServer microservicesServer) {
        httpTransports.clear();
        LOGGER.debug("Microservices Server '{}' unregistered.", microservicesServer.getClass().getName());
    }

    /**
     * Registers supplied Microservice to the specified HTTP transport for the specified content path.
     *
     * @param microservice            Microservice to be registered
     * @param contextPath             context path
     * @param listenerConfigurationId listener configuration ID of the transport that the supplying Microservice should
     *                                be registered
     * @return Microservice registration object that represents this registration
     * @throws IllegalArgumentException if cannot find a HTTP transport for the {@code listenerConfigurationId}
     */
    public MicroserviceRegistration register(Microservice microservice, String contextPath,
                                             String listenerConfigurationId) throws IllegalArgumentException {
        HttpTransport httpTransport = httpTransports.stream()
                .filter(ht -> Objects.equals(ht.getListenerConfigurationId(), listenerConfigurationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cannot find a HTTP transport for listener configuration ID '" +
                        listenerConfigurationId + "'. Available HTTP transport: " + httpTransports));
        ServiceRegistration<Microservice> serviceRegistration = register(microservice, contextPath,
                                                                         httpTransport);
        return new MicroserviceRegistration(httpTransport, serviceRegistration);
    }

    /**
     * Registers supplied Microservice to all available <b>HTTPS</b> transports for the specified content path.
     *
     * @param microservice Microservice to be registered
     * @param contextPath  context path
     * @return set of Microservice registrations object that represents registrations
     */
    public Set<MicroserviceRegistration> register(Microservice microservice, String contextPath) {
        return httpTransports.stream()
                .filter(HttpTransport::isSecured)
                .map(ht -> new MicroserviceRegistration(ht, register(microservice, contextPath, ht)))
                .collect(Collectors.toSet());
    }

    private ServiceRegistration<Microservice> register(Microservice microservice, String contextPath,
                                                       HttpTransport httpTransport) {
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("LISTENER_INTERFACE_ID", httpTransport.getListenerInterfaceId());
        properties.put("contextPath", contextPath);
        properties.put("skipCarbonStartupResolver", true);
        ServiceRegistration<Microservice> registration = bundleContext.registerService(Microservice.class, microservice,
                                                                                       properties);
        LOGGER.debug("Microservice {} registered to {} for context path '{}'.", microservice, httpTransport,
                     contextPath);
        return registration;
    }
}
