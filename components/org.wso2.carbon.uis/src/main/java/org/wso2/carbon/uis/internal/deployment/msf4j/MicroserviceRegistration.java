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

package org.wso2.carbon.uis.internal.deployment.msf4j;

import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uis.internal.http.HttpTransport;
import org.wso2.msf4j.Microservice;

import java.util.Objects;

/**
 * Represents a Microservice registration to the HTTP transport(s).
 *
 * @since 0.15.0
 */
public class MicroserviceRegistration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceRegistration.class);

    private final HttpTransport httpTransport;
    private final ServiceRegistration<Microservice> microserviceRegistration;

    /**
     * Creates a new Microservice service registration.
     *
     * @param httpTransport            HTTP transport
     * @param microserviceRegistration Microservice OSGi service registration
     */
    public MicroserviceRegistration(HttpTransport httpTransport,
                                    ServiceRegistration<Microservice> microserviceRegistration) {
        this.httpTransport = httpTransport;
        this.microserviceRegistration = microserviceRegistration;
    }

    /**
     * Returns the HTTP transport that this registration occurred.
     *
     * @return relevant HTTP transport
     */
    public HttpTransport getRegisteredHttpTransport() {
        return httpTransport;
    }

    /**
     * Unregister the Microservice that associated with this registration from the transport(s).
     *
     * @throws IllegalStateException if Microservice is already unregistered
     */
    public void unregister() {
        microserviceRegistration.unregister();
        LOGGER.debug("Microservice unregistered from HTTP transport {}.", httpTransport);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MicroserviceRegistration)) {
            return false;
        }
        MicroserviceRegistration other = (MicroserviceRegistration) obj;
        return Objects.equals(httpTransport, other.httpTransport) &&
               Objects.equals(microserviceRegistration, other.microserviceRegistration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(httpTransport, microserviceRegistration);
    }

    @Override
    public String toString() {
        return "MicroserviceRegistration{httpTransport=" + httpTransport + ", microserviceRegistration=" +
               microserviceRegistration + "}";
    }
}
