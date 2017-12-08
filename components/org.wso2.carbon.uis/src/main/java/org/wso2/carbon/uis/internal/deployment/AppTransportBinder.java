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
import org.wso2.carbon.uis.internal.http.HttpTransport;
import org.wso2.carbon.uis.internal.http.RequestDispatcher;
import org.wso2.carbon.uis.internal.http.msf4j.MicroserviceRegistration;
import org.wso2.carbon.uis.internal.http.msf4j.MicroservicesRegistrar;
import org.wso2.carbon.uis.internal.http.msf4j.WebappMicroservice;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
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
    private final ConcurrentMap<String, MicroserviceRegistration> microserviceRegistrations;

    /**
     * Creates a new app transport binder.
     *
     * @param microservicesRegistrar Microservices registrar
     */
    public AppTransportBinder(MicroservicesRegistrar microservicesRegistrar) {
        this.microservicesRegistrar = microservicesRegistrar;
        this.microserviceRegistrations = new ConcurrentHashMap<>();
    }

    @Override
    public void appDeploymentEvent(App app) {
        String appContextPath = app.getContextPath();
        MicroserviceRegistration registration;
        if (app.getConfiguration().isHttpsOnly()) {
            registration = microservicesRegistrar.registerSecuredMicroservice(createMicroservice(app), appContextPath);
        } else {
            registration = microservicesRegistrar.registerMicroservice(createMicroservice(app), appContextPath);
        }

        for (HttpTransport httpTransport : registration.getRegisteredHttpTransports()) {
            LOGGER.info("Web app '{}' is available at '{}'.", app.getName(), getAppUrl(appContextPath, httpTransport));
        }
        microserviceRegistrations.put(app.getName(), registration);
    }

    @Override
    public void appUndeploymentEvent(String appName) {
        MicroserviceRegistration registration = microserviceRegistrations.remove(appName);
        if (registration == null) {
            throw new IllegalArgumentException("Cannot unregister web app '" + appName +
                                               "'. App might be already unregistered or not be registered at all.");
        }

        registration.unregister();
        LOGGER.info("Web app '{}' undeployed.", appName);
    }

    /**
     * Closes this binder.
     */
    public void close() {
        microserviceRegistrations.values().forEach(MicroserviceRegistration::unregister);
        microserviceRegistrations.clear();
    }

    private static WebappMicroservice createMicroservice(App app) {
        RequestDispatcher requestDispatcher = new RequestDispatcher(app);
        return new WebappMicroservice((requestDispatcher::serve));
    }

    /**
     * Returns a accessible URL for the given app content path.
     *
     * @param appContextPath content path of the app
     * @param httpTransport  HTTP transport that the relevant app is bound
     * @return
     */
    private static String getAppUrl(String appContextPath, HttpTransport httpTransport) {
        String hostname = httpTransport.getHost().trim();
        // We can safely continue with the hostname from HTTP transport, if following if-block breaks at any point.
        if ("localhost".equals(hostname) || "127.0.0.1".equals(hostname) || "0.0.0.0".equals(hostname) ||
            "::1".equals(hostname)) {
            try {
                Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (networkInterfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = networkInterfaces.nextElement();
                    if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                        continue;
                    }

                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (((inetAddress instanceof Inet4Address) || (inetAddress instanceof Inet6Address)) &&
                            !inetAddress.isLoopbackAddress()) {
                            hostname = inetAddress.getHostAddress();
                        }
                    }

                }
            } catch (SocketException e) {
                // Log level DEBUG since this is not a 'breaking' error.
                LOGGER.debug("Cannot access information on network interfaces.", e);
            }
        }

        return httpTransport.getScheme() + "://" + hostname + ":" + httpTransport.getPort() + appContextPath;
    }
}
