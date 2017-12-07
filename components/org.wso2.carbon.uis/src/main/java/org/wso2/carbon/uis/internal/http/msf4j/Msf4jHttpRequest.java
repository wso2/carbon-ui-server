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

import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.uis.api.http.HttpRequest;
import org.wso2.msf4j.Request;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.ws.rs.core.HttpHeaders;

/**
 * MSF4J based implementation of HTTP request.
 *
 * @since 0.8.0
 */
public class Msf4jHttpRequest implements HttpRequest {

    private static final String PROPERTY_HTTP_VERSION = "HTTP_VERSION";
    private static final String PROPERTY_IS_SECURED_CONNECTION = "IS_SECURED_CONNECTION";

    private final Request msf4jRequest;
    private final String method;
    private final Map<String, Cookie> cookies;
    private final Map<String, String> headers;
    private final String uri;
    private final String contextPath;
    private final String uriWithoutContextPath;
    private final String queryString;
    private final Map<String, List<String>> queryParams;

    /**
     * Creates a new GET {@link HttpRequest} from the MSF4J request.
     *
     * @param request MSF4J request
     */
    public Msf4jHttpRequest(Request request) {
        this.msf4jRequest = request;
        this.method = request.getHttpMethod();

        // process URI
        String rawUri = request.getUri();
        int uriPathEndIndex = rawUri.indexOf('?');
        String rawUriPath, rawQueryString;
        if (uriPathEndIndex == -1) {
            rawUriPath = rawUri;
            rawQueryString = null;
        } else {
            rawUriPath = rawUri.substring(0, uriPathEndIndex);
            rawQueryString = rawUri.substring(uriPathEndIndex + 1, rawUri.length());
        }
        this.uri = QueryStringDecoder.decodeComponent(rawUriPath);
        this.contextPath = HttpRequest.getContextPath(this.uri);
        this.uriWithoutContextPath = HttpRequest.getUriWithoutContextPath(this.uri);
        this.queryString = rawQueryString; // Query string is not very useful, so we don't bother to decode it.
        if (rawQueryString != null) {
            this.queryParams = Collections.unmodifiableMap(new QueryStringDecoder(rawQueryString, false).parameters());
        } else {
            this.queryParams = Collections.emptyMap();
        }

        // process headers and cookies
        Map<String, String> httpHeaders = request.getHeaders().getAll().stream()
                .collect(Collectors.toMap(Header::getName, Header::getValue));
        this.headers = Collections.unmodifiableMap(httpHeaders);
        String cookieHeader = httpHeaders.get(HttpHeaders.COOKIE);
        this.cookies = (cookieHeader == null) ? Collections.emptyMap() :
                ServerCookieDecoder.STRICT.decode(cookieHeader).stream().collect(Collectors.toMap(Cookie::name,
                                                                                                  Function.identity()));
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getProtocol() {
        return (String) msf4jRequest.getProperty(PROPERTY_HTTP_VERSION);
    }

    @Override
    public boolean isSecure() {
        return (Boolean) msf4jRequest.getProperty(PROPERTY_IS_SECURED_CONNECTION);
    }

    @Override
    public String getUrl() {
        throw new UnsupportedOperationException("To be implemented");
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public String getUriWithoutContextPath() {
        return uriWithoutContextPath;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String getCookieValue(String cookieName) {
        Cookie cookie = cookies.get(cookieName);
        return (cookie == null) ? null : cookie.value();
    }

    @Override
    public String toString() {
        return "{\"method\": \"" + method + "\", \"uri\": \"" + uri + "\", \"query\": \"" + queryString +
               "\", \"protocol\": \"" + getProtocol() + "\"}";
    }
}
