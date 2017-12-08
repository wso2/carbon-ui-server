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

import java.util.Locale;
import java.util.Objects;

/**
 * Modal class that holds information about a HTTP transport.
 *
 * @since 0.15.0
 */
public class HttpTransport {

    private final String id;
    private final String scheme;
    private final String host;
    private final int port;

    /**
     * Creates a new HTTP transport.
     *
     * @param id     ID of the transport
     * @param scheme scheme of the transport (either {@code http} or {@code https})
     * @param host   host name of the transport
     * @param port   port of the transport
     */
    public HttpTransport(String id, String scheme, String host, int port) {
        this.id = id;
        this.scheme = scheme.toLowerCase(Locale.ENGLISH);
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
        return Objects.equals(scheme, "https");
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
}
