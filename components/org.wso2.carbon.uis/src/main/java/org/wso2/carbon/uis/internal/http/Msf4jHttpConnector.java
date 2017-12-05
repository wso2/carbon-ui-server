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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
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
import org.wso2.carbon.uis.api.App;
import org.wso2.carbon.uis.api.http.HttpConnector;
import org.wso2.carbon.uis.api.http.HttpRequest;
import org.wso2.carbon.uis.api.http.HttpResponse;
import org.wso2.msf4j.Microservice;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * MSF4J based implementation of HTTP connector.
 *
 * @since 0.8.0
 */
@Component(service = HttpConnector.class,
           immediate = true)
public class Msf4jHttpConnector implements HttpConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(Msf4jHttpConnector.class);

    private final Set<HttpTransport> httpTransports;
    private final SetMultimap<String, ServiceRegistration<Microservice>> microserviceRegistrations;
    private BundleContext bundleContext;

    /**
     * Creates a new MSF4J HTTP connector.
     */
    public Msf4jHttpConnector() {
        this.httpTransports = new HashSet<>();
        this.microserviceRegistrations = HashMultimap.create();
    }

    @Reference(service = ServerConnector.class,
               cardinality = ReferenceCardinality.AT_LEAST_ONE,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unsetCarbonTransport")
    protected void setCarbonTransport(ServerConnector serverConnector) {
        if (serverConnector instanceof HTTPServerConnector) {
            HTTPServerConnector httpServerConnector = (HTTPServerConnector) serverConnector;
            HttpTransport httpTransport = HttpTransport.toHttpTransport(httpServerConnector);
            httpTransports.add(httpTransport);
            LOGGER.debug("HTTP transport '{}' registered via '{}' to Microservices HTTP connector.",
                         httpTransport.getId(), serverConnector.getClass().getName());
        }
    }

    protected void unsetCarbonTransport(ServerConnector serverConnector) {
        if (serverConnector instanceof HTTPServerConnector) {
            HTTPServerConnector httpServerConnector = (HTTPServerConnector) serverConnector;
            HttpTransport httpTransport = HttpTransport.toHttpTransport(httpServerConnector);
            httpTransports.remove(httpTransport);
            LOGGER.debug("HTTP transport '{}' unregistered via '{}' from Microservices HTTP connector.",
                         httpTransport.getId(), serverConnector.getClass().getName());
        }
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        LOGGER.debug("MSF4J HTTP connector activated.");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        this.bundleContext = null;
        LOGGER.debug("MSF4J HTTP connector deactivated.");
    }

    @Override
    public void registerApp(App app, Function<HttpRequest, HttpResponse> httpListener) {
        httpTransports.stream()
                .filter(httpTransport -> !app.getConfiguration().isHttpsOnly() || httpTransport.isSecured())
                .forEach(httpTransport -> {
                    Dictionary<String, String> properties = new Hashtable<>();
                    properties.put("CHANNEL_ID", httpTransport.getId());
                    properties.put("contextPath", app.getContextPath());
                    ServiceRegistration<Microservice> serviceRegistration = bundleContext.registerService(
                            Microservice.class, new WebappMicroservice(httpListener), properties);

                    microserviceRegistrations.put(app.getName(), serviceRegistration);
                    LOGGER.info("Web app '{}' is available at '{}'.", app.getName(),
                                getAppUrl(app, httpTransport));
                });
    }

    @Override
    public void unregisterApp(String appName) {
        Set<ServiceRegistration<Microservice>> registrations = microserviceRegistrations.removeAll(appName);
        if (registrations.isEmpty()) {
            throw new IllegalArgumentException("Cannot unregister web app '" + appName +
                                               "'. App might be already unregistered or not be registered at all.");
        }

        registrations.forEach(ServiceRegistration::unregister);
        LOGGER.info("Web app '{}' undeployed.", appName);
    }

    @Override
    public void unregisterAllApps() {
        microserviceRegistrations.keySet().forEach(this::unregisterApp);
    }

    /**
     * Returns URL for the specified app.
     *
     * @param app           web app
     * @param httpTransport HTTP transport
     * @return an URL that use to access the specified app
     */
    private static String getAppUrl(App app, HttpTransport httpTransport) {
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

        return httpTransport.getScheme() + "://" + hostname + ":" + httpTransport.getPort() + app.getContextPath();
    }

    /**
     * Modal class that holds information about a HTTP transport.
     *
     * @since 0.8.2
     */
    private static class HttpTransport {

        private final String id;
        private final String scheme;
        private final String host;
        private final int port;

        public HttpTransport(String id, String scheme, String host, int port) {
            this.id = id;
            this.scheme = scheme;
            this.host = host;
            this.port = port;
        }

        /**
         * Returns the ID of the represented HTTP transport.
         *
         * @return ID of the HTTP transport
         */
        public String getId() {
            return id;
        }

        /**
         * Returns the scheme of the represented HTTP transport.
         *
         * @return scheme of the HTTP transport
         */
        public String getScheme() {
            return scheme;
        }

        /**
         * Returns the host of the represented HTTP transport.
         *
         * @return host of the HTTP transport
         */
        public String getHost() {
            return host;
        }

        /**
         * Returns the port of the represented HTTP transport.
         *
         * @return port of the HTTP transport
         */
        public int getPort() {
            return port;
        }

        /**
         * Returns whether the represented HTTP transport is secured or not
         *
         * @return {@code true} if the {@link #getScheme() scheme} is HTTPS, otherwise {@code false}
         */
        public boolean isSecured() {
            return scheme.equalsIgnoreCase("https");
        }

        @Override
        public boolean equals(Object obj) {
            return (this == obj) || ((obj instanceof HttpTransport) && Objects.equals(id, ((HttpTransport) obj).id));
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return "HttpTransport{id='" + id + "', scheme='" + scheme + "', host='" + host + "', port='" + port + "'}";
        }

        /**
         * Returns the HTTP transport information of the specified HTTP server connector.
         *
         * @param httpServerConnector HTTP server connector to be represented
         * @return HTTP transport information
         */
        public static HttpTransport toHttpTransport(HTTPServerConnector httpServerConnector) {
            ListenerConfiguration config = httpServerConnector.getListenerConfiguration();
            return new HttpTransport(config.getId(), config.getScheme(), config.getHost(), config.getPort());
        }
    }
}
