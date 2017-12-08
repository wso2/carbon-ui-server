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

import org.apache.commons.lang3.tuple.Pair;
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
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.listener.HTTPServerConnector;
import org.wso2.carbon.uis.internal.http.HttpTransport;
import org.wso2.msf4j.Microservice;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
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

    @Reference(service = ServerConnector.class,
               cardinality = ReferenceCardinality.AT_LEAST_ONE,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetCarbonTransport")
    protected void setCarbonTransport(ServerConnector serverConnector) {
        if (serverConnector instanceof HTTPServerConnector) {
            HTTPServerConnector httpServerConnector = (HTTPServerConnector) serverConnector;
            HttpTransport httpTransport = toHttpTransport(httpServerConnector);
            httpTransports.add(httpTransport);
            LOGGER.debug("HTTP transport '{}' registered via server connector '{}'.",
                        httpTransport.getId(), serverConnector.getClass().getName());
        }
    }

    protected void unsetCarbonTransport(ServerConnector serverConnector) {
        if (serverConnector instanceof HTTPServerConnector) {
            HTTPServerConnector httpServerConnector = (HTTPServerConnector) serverConnector;
            HttpTransport httpTransport = toHttpTransport(httpServerConnector);
            httpTransports.remove(httpTransport);
            LOGGER.debug("HTTP transport '{}' unregistered via server connector '{}'.",
                        httpTransport.getId(), serverConnector.getClass().getName());
        }
    }

    /**
     * Registers supplied Microservice to available HTTP transports for the specified content path.
     *
     * @param microservice Microservice to be registered
     * @param contextPath  context path
     * @return Microservice registration object that represents this registration
     */
    public MicroserviceRegistration registerMicroservice(Microservice microservice, String contextPath) {
        return registerMicroservice(microservice, contextPath, false);
    }

    /**
     * Registers supplied Microservice to available <b>HTTPS</b> transports for the specified content path.
     *
     * @param microservice Microservice to be registered
     * @param contextPath  context path
     * @return Microservice registration object that represents this registration
     */
    public MicroserviceRegistration registerSecuredMicroservice(Microservice microservice, String contextPath) {
        return registerMicroservice(microservice, contextPath, true);
    }

    private MicroserviceRegistration registerMicroservice(Microservice microservice, String contextPath,
                                                          boolean isHttpsOnly) {
        Map<HttpTransport, ServiceRegistration<Microservice>> serviceRegistrations = httpTransports.stream()
                .filter(httpTransport -> !isHttpsOnly || httpTransport.isSecured())
                .map(httpTransport -> Pair.of(httpTransport,
                                              registerMicroservice(contextPath, microservice, httpTransport)))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        return new MicroserviceRegistration(serviceRegistrations);
    }

    private ServiceRegistration<Microservice> registerMicroservice(String contextPath, Microservice microservice,
                                                                   HttpTransport httpTransport) {
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("CHANNEL_ID", httpTransport.getId());
        properties.put("contextPath", contextPath);
        properties.put("skipCarbonStartupResolver", true);
        return bundleContext.registerService(Microservice.class, microservice, properties);

    }

    private static HttpTransport toHttpTransport(HTTPServerConnector httpServerConnector) {
        ListenerConfiguration config = httpServerConnector.getListenerConfiguration();
        return new HttpTransport(config.getId(), config.getScheme(), config.getHost(), config.getPort());
    }

}
