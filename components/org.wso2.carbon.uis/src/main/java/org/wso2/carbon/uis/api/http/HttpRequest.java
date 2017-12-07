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

package org.wso2.carbon.uis.api.http;

import java.util.List;
import java.util.Map;

/**
 * Represent a HTTP request.
 *
 * @since 0.8.0
 */
public interface HttpRequest {

    /**
     * Returns the name of the HTTP method with which this request was made, for example, GET, POST.
     *
     * @return a {@code String} specifying the name of the method with which this request was made
     */
    String getMethod();

    /**
     * Returns the name and version of the protocol the request uses in the form <i>protocol/majorVersion
     * .minorVersion</i>, for example, HTTP/1.1.
     *
     * @return a {@code String} containing the protocol name and version number
     */
    String getProtocol();

    /**
     * Returns a boolean indicating whether this request was made using a secure channel, i.e. HTTPS.
     *
     * @return {@code true} when HTTPS, otherwise {@code false}
     */
    boolean isSecure();

    /**
     * Returns the name of the scheme used to make this request.
     *
     * @return either "{@code http}" or "{@code https}"
     */
    default String getScheme() {
        return (isSecure() ? "https" : "http");
    }

    /**
     * Reconstructs the URL the client used to make the request.
     *
     * @return request url
     */
    String getUrl();

    /**
     * Returns the part of this request's URL from the protocol name up to the query string in the first line of the
     * HTTP request. For example:
     * <br><br>
     * <table summary="Examples of Returned Values"> <tr align=left> <th>First line of HTTP request</th> <th>Returned
     * Value</th> </tr> <tr> <td>GET http://foo.bar/a/b.html HTTP/1.0</td> <td>/a/b.html</td> </tr> <tr> <td>GET
     * https://192.168.1.1:9292/foo/bar?x=y HTTP/1.1</td> <td>/foo/bar</td> </tr> <tr> <td>POST /some/path.html
     * HTTP/1.1</td> <td>/some/path.html</td> </tr> </table>
     * <br>
     * Returned URI string is decoded.
     *
     * @return a decoded {@code String} containing the part of the URL from the protocol name up to the query string
     */
    String getUri();

    /**
     * Returns the part of this request's URI from the first forward slash up to the second forward slash but not
     * including. For example.
     * <br><br>
     * <table summary="Examples of Returned Values"> <tr align=left> <th>URI</th> <th>Returned Value</th> </tr> <tr>
     * <td>/a/b.html</td> <td>/a</td> </tr> <tr> <td>/foo/bar?x=y</td> <td>/foo</td> </tr> <tr> <td>/some/path.html</td>
     * <td>/some</td> </tr> </table>
     *
     * @return a {@code String} containing the part of the URI from the first forward slash up to the second forward
     * slash
     * @see #getUri()
     */
    String getContextPath();

    /**
     * Returns the part of this request's URI from the second forward slash to the end. For example:
     * <br><br>
     * <table summary="Examples of Returned Values"> <tr align=left> <th>URI</th> <th>Returned Value</th> </tr> <tr>
     * <td>/a/b.html</td> <td>/b.html</td> </tr> <tr> <td>/foo/bar?x=y</td> <td>/bar</td> </tr> <tr>
     * <td>/some/path.html</td> <td>/path.html</td> </tr> </table>
     *
     * @return a {@code String} that contains the remaining of the URI after removing the context path from it
     * @see #getUri()
     * @see #getContextPath()
     */
    String getUriWithoutContextPath();

    /**
     * Returns the query string that is contained in the request URL after the path. This method returns {@code null} if
     * the URL does not have a query string.
     *
     * @return an un-decoded {@code String} containing the query string or {@code null} if the URL contains no query
     * string.
     * @see #getUri()
     */
    String getQueryString();

    /**
     * Returns query parameters of this request. All keys and values of the returned map are decoded.
     *
     * @return a map containing parameter names as keys and parameter values as map values
     */
    Map<String, List<String>> getQueryParams();

    /**
     * Returns all HTTP headers of this request.
     *
     * @return HTTP headers
     */
    Map<String, String> getHeaders();

    /**
     * Retuns the value of the specified Cookie.
     *
     * @param cookieName name of the Cookie
     * @return value of the Cookie or {@code null} if a Cookie with the specified name doesn't exist
     */
    String getCookieValue(String cookieName);

    /**
     * Returns a string representation of this request.
     *
     * @return a string representation of this request
     */
    String toString();

    /**
     * Returns whether this is a valid request or not.
     *
     * @return {@code true} if this is a valid request, {@code false} if not
     */
    default boolean isValid() {
        String uri = getUri();

        // An URI must begin with '/' & it should have at least two characters.
        if ((uri == null) || (uri.length() < 2) || (uri.charAt(0) != '/')) {
            return false;
        }

        // '//' or '..' are not allowed in URIs.
        boolean isPreviousCharInvalid = false;
        for (int i = 0; i < uri.length(); i++) {
            char currentChar = uri.charAt(i);
            if ((currentChar == '/') || (currentChar == '.')) {
                if (isPreviousCharInvalid) {
                    return false;
                } else {
                    isPreviousCharInvalid = true;
                }
            } else {
                isPreviousCharInvalid = false;
            }
        }
        return true;
    }

    /**
     * Returns whether this request is for a static resource.
     *
     * @return {@code true} if this is a request to a static resource, {@code false} if not
     */
    default boolean isStaticResourceRequest() {
        return getUriWithoutContextPath().startsWith("/public/");
    }

    /**
     * Returns whether this request is for a static resource in an app.
     *
     * @return {@code true} if this is a request to a static resource in an app, {@code false} if not
     */
    default boolean isAppStaticResourceRequest() {
        return getUriWithoutContextPath().startsWith("/public/app/");
    }

    /**
     * Returns whether this request is for a static resource in an extension.
     *
     * @return {@code true} if this is a request to a static resource in an extension, {@code false} if not
     */
    default boolean isExtensionStaticResourceRequest() {
        return getUriWithoutContextPath().startsWith("/public/extensions/");
    }

    /**
     * Returns whether this request is for a static resource in a theme.
     *
     * @return {@code true} if this is a request to a static resource in a theme, {@code false} if not
     */
    default boolean isThemeStaticResourceRequest() {
        return getUriWithoutContextPath().startsWith("/public/themes/");
    }

    /**
     * Returns whether this request is for the default favicon.
     *
     * @return {@code true} if this is a request to the default favicon, {@code false} if not
     */
    default boolean isDefaultFaviconRequest() {
        return getUri().equals("/favicon.ico");
    }

    /**
     * Returns the context path of the specified URI.
     *
     * @param uri URI
     * @return context path of the specified URI
     * @see #getContextPath()
     */
    static String getContextPath(String uri) {
        int secondSlash = uri.indexOf('/', 1); // An URI must start with a slash.
        if (secondSlash == -1) {
            // There is only one slash in the URI.
            return uri;
        } else {
            return uri.substring(0, secondSlash);
        }
    }

    /**
     * Returns the part of the specified URI from the end of the context path to the end of the URI.
     *
     * @param uri URI
     * @return part of the URI without the context path
     * @see #getUriWithoutContextPath()
     */
    static String getUriWithoutContextPath(String uri) {
        int secondSlash = uri.indexOf('/', 1); // An URI must start with a slash.
        if (secondSlash == -1) {
            // There is only one slash in the URI.
            return "";
        } else {
            return uri.substring(secondSlash, uri.length());
        }
    }
}
