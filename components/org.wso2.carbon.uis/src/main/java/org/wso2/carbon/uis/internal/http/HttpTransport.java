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

import org.wso2.carbon.uis.internal.http.util.IpAddressUtils;

import java.util.Locale;
import java.util.Objects;

/**
 * Modal class that holds information about a HTTP transport.
 *
 * @since 0.15.0
 */
public class HttpTransport {

    private final String listenerInterfaceId;
    private final String listenerConfigurationId;
    private final String scheme;
    private final String host;
    private final int port;

    /**
     * Creates a new HTTP transport.
     *
     * @param listenerInterfaceId     ID of the listener interface
     * @param listenerConfigurationId ID of the listener configuration
     * @param scheme                  scheme of the transport (either {@code http} or {@code https})
     * @param host                    host name of the transport
     * @param port                    port of the transport
     */
    public HttpTransport(String listenerInterfaceId, String listenerConfigurationId,
                         String scheme, String host, int port) {
        this.listenerInterfaceId = listenerInterfaceId;
        this.listenerConfigurationId = listenerConfigurationId;
        this.scheme = scheme.toLowerCase(Locale.ENGLISH);
        this.host = host;
        this.port = port;
    }

    /**
     * Returns the ID of the listener interface of the represented HTTP transport
     *
     * @return ID of the listener interface
     */
    public String getListenerInterfaceId() {
        return listenerInterfaceId;
    }

    /**
     * Returns the ID of the listener configuration of the represented HTTP transport
     *
     * @return ID of the listener configuration
     */
    public String getListenerConfigurationId() {
        return listenerConfigurationId;
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
        return Objects.equals(scheme, "https");
    }

    /**
     * Returns an accessible URL for the given context path via this HTTP transport.
     *
     * @param contextPath content path of the URL
     * @return URL
     */
    public String getUrlFor(String contextPath) {
        String properHost = host;
        if ("localhost".equals(host) || "127.0.0.1".equals(host) || "0.0.0.0".equals(host) || "::1".equals(host)) {
            properHost = IpAddressUtils.getLocalIpAddress().orElse(host);
        }
        return scheme + "://" + properHost + ":" + port + contextPath;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HttpTransport)) {
            return false;
        }
        HttpTransport other = (HttpTransport) obj;
        return Objects.equals(listenerInterfaceId, other.listenerInterfaceId) &&
               Objects.equals(listenerConfigurationId, other.listenerConfigurationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(listenerInterfaceId, listenerConfigurationId);
    }

    @Override
    public String toString() {
        return "HttpTransport{listenerInterfaceId='" + listenerInterfaceId + "', listenerConfigurationId='" +
               listenerConfigurationId + "', scheme='" + scheme + "', host='" + host + "', port=" + port + '}';
    }
}
