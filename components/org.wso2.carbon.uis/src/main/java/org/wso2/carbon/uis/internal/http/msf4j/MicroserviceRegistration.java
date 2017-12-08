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

import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uis.internal.http.HttpTransport;
import org.wso2.msf4j.Microservice;

import java.util.Map;
import java.util.Set;

/**
 * Represents a Microservice registration to the HTTP transport(s).
 *
 * @since 0.15.0
 */
public class MicroserviceRegistration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceRegistration.class);

    private final Map<HttpTransport, ServiceRegistration<Microservice>> microserviceRegistrations;

    MicroserviceRegistration(Map<HttpTransport, ServiceRegistration<Microservice>> microserviceRegistrations) {
        this.microserviceRegistrations = microserviceRegistrations;
    }

    /**
     * Returns HTTP transports that this registration occurred.
     *
     * @return HTTP transports that the registration occurred
     */
    public Set<HttpTransport> getRegisteredHttpTransports() {
        return microserviceRegistrations.keySet();
    }

    /**
     * Unregister the Microservice that associated with this registration from the transport(s).
     *
     * @throws IllegalStateException if Microservice is already unregistered
     */
    public void unregister() {
        microserviceRegistrations.forEach((httpTransport, microserviceServiceRegistration) -> {
            microserviceServiceRegistration.unregister();
            LOGGER.debug("Microservice unregistered from HTTP transport {}.", httpTransport);
        });
    }
}
