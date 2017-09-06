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

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.listener.HTTPServerConnector;
import org.wso2.carbon.uis.api.http.HttpConnector;
import org.wso2.carbon.uis.api.http.HttpRequest;
import org.wso2.carbon.uis.api.http.HttpResponse;
import org.wso2.msf4j.Microservice;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * MSF4J based implementation of HTTP connector.
 *
 * @since 0.8.0
 */
@Component(name = "org.wso2.carbon.uis.internal.http.Msf4jHttpConnector",
           service = HttpConnector.class,
           immediate = true
)
public class Msf4jHttpConnector implements HttpConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(Msf4jHttpConnector.class);

    private Set<HttpTransport> httpTransports = new HashSet<>();
    private BundleContext bundleContext;

    @Reference(
            name = "http-connector-provider",
            service = ServerConnector.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCarbonTransport"
    )
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

    @Reference(
            name = "carbon-server-info",
            service = CarbonServerInfo.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCarbonServerInfo"
    )
    protected void setCarbonServerInfo(CarbonServerInfo carbonServerInfo) {
        LOGGER.info("CarbonServerInfo instance '{}' registered.", carbonServerInfo.getClass().getName());
    }

    protected void unsetCarbonServerInfo(CarbonServerInfo carbonServerInfo) {
        LOGGER.info("CarbonServerInfo instance '{}' unregistered.", carbonServerInfo.getClass().getName());
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        LOGGER.debug("Microservices HTTP connector activated.");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        this.bundleContext = null;
        LOGGER.debug("Microservices HTTP connector deactivated.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerApp(String appName, String appContextPath, Function<HttpRequest, HttpResponse> httpListener) {
        for (HttpTransport httpTransport : httpTransports) {
            Dictionary<String, String> dictionary = new Hashtable<>();
            dictionary.put("CHANNEL_ID", httpTransport.getId());
            dictionary.put("contextPath", appContextPath);
            bundleContext.registerService(Microservice.class, new WebappMicroservice(httpListener), dictionary);
            LOGGER.info("Webapp '{}' is available at '{}'.", appName, httpTransport.getAppUrl(appContextPath));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerApps(Map<String, String> appNamesContextPaths,
                             Function<HttpRequest, HttpResponse> httpListener) {
        for (Map.Entry<String, String> appNameContextPath : appNamesContextPaths.entrySet()) {
            registerApp(appNameContextPath.getKey(), appNameContextPath.getValue(), httpListener);
        }
    }

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

        public String getId() {
            return id;
        }

        public String getScheme() {
            return scheme;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public boolean isSecured() {
            return host.equalsIgnoreCase("https");
        }

        public String getAppUrl(String appContextPath) {
            return scheme + "://" + host + ":" + port + appContextPath + "/";
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

        public static HttpTransport toHttpTransport(HTTPServerConnector httpServerConnector) {
            ListenerConfiguration config = httpServerConnector.getListenerConfiguration();
            return new HttpTransport(config.getId(), config.getScheme(), config.getHost(), config.getPort());
        }
    }
}
