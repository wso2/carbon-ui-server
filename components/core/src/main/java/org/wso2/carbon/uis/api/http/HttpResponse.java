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

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Represents a HTTP response.
 *
 * @since 1.0.0
 */
public class HttpResponse {

    public static final int STATUS_OK = 200;
    public static final int STATUS_MOVED_PERMANENTLY = 301;
    public static final int STATUS_FOUND = 302;
    public static final int STATUS_NOT_MODIFIED = 304;
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_UNAUTHORIZED = 401;
    public static final int STATUS_FORBIDDEN = 403;
    public static final int STATUS_NOT_FOUND = 404;
    public static final int STATUS_INTERNAL_SERVER_ERROR = 500;

    public static final String CONTENT_TYPE_WILDCARD = "*/*";
    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    public static final String CONTENT_TYPE_TEXT_HTML = "text/html";
    public static final String CONTENT_TYPE_IMAGE_PNG = "image/png";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

    public static final String HEADER_LOCATION = "Location";
    public static final String HEADER_X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    public static final String HEADER_X_XSS_PROTECTION = "X-XSS-Protection";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";
    public static final String HEADER_EXPIRES = "Expires";
    public static final String HEADER_PRAGMA = "Pragma";
    public static final String HEADER_X_FRAME_OPTIONS = "X-Frame-Options";

    private int status;
    private Object content;
    private String contentType;
    private MultivaluedMap<String, String> headers;
    private Map<String, String> cookies;

    public HttpResponse() {
        this.status = 200;
        this.headers = new MultivaluedHashMap<>();
        this.cookies = new HashMap<>();
    }

    /**
     * Sets the <a href="https://tools.ietf.org/html/rfc2616#section-10">HTTP status code</a> of this response to the
     * specified integer.
     *
     * @param statusCode HTTP status code to be set
     */
    public void setStatus(int statusCode) {
        this.status = statusCode;
    }

    /**
     * Returns the <a href="https://tools.ietf.org/html/rfc2616#section-10">HTTP status code</a> of this response.
     *
     * @return HTTP status code of this response
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets the specified textual content to this response. This is equivalent to
     * {@code setContent(content, }{@link #CONTENT_TYPE_TEXT_PLAIN}{@code )}
     *
     * @param content textual content to be set
     * @see #setContent(String, String)
     */
    public void setContent(String content) {
        setContent(content, CONTENT_TYPE_TEXT_PLAIN);
    }

    /**
     * Sets the specified textual content and the content type to this response.
     *
     * @param content     textual content to be set
     * @param contentType MIME type of the content
     */
    public void setContent(String content, String contentType) {
        setContent((Object) content, contentType);
    }

    /**
     * Sets the file content located by the specified path to this response.
     *
     * @param content path of the file content to be set
     */
    public void setContent(Path content) {
        setContent(content.toFile());
    }

    /**
     * Sets the file content located by the specified path and the content type to this response.
     *
     * @param content     path of the file content to be set
     * @param contentType MIME type of the content
     */
    public void setContent(Path content, String contentType) {
        setContent(content.toFile(), contentType);
    }

    /**
     * Sets the specified file content to this response.
     *
     * @param content file content to be set
     */
    public void setContent(File content) {
        String extension = FilenameUtils.getExtension(content.getName());
        setContent(content, extension.isEmpty() ? CONTENT_TYPE_WILDCARD : extension);
    }

    /**
     * Sets the specified file content and the content type to this response.
     *
     * @param content     file content to be set
     * @param contentType MIME type of the content
     */
    public void setContent(File content, String contentType) {
        setContent((Object) content, contentType);
    }

    /**
     * Sets the content read through the specified input stream and the content type to this response.
     *
     * @param content     input stream to the content to be set
     * @param contentType MIME type of the content
     */
    public void setContent(InputStream content, String contentType) {
        setContent((Object) content, contentType);
    }

    /**
     * Sets the specified content and the content type to this response.
     *
     * @param content     content to be set
     * @param contentType MIME type of the content
     */
    public void setContent(Object content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    /**
     * Returns the content of this response.
     *
     * @return the content of this response
     */
    public Object getContent() {
        return content;
    }

    /**
     * Returns the <a href="https://www.iana.org/assignments/media-types/media-types.xhtml">MIME type</a> of this
     * response.
     *
     * @return MIME type of this response
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the specified the HTTP status code and the textual content to this response.
     *
     * @param statusCode HTTP status code to be set
     * @param content    textual content to be set
     */
    public void setContent(int statusCode, String content) {
        setStatus(statusCode);
        setContent(content);
    }

    /**
     * Sets the specified the HTTP status code and the textual content to this response.
     *
     * @param statusCode  HTTP status code to be set
     * @param content     textual content to be set
     * @param contentType MIME type of the content
     */
    public void setContent(int statusCode, String content, String contentType) {
        setStatus(statusCode);
        setContent(content, contentType);
    }

    /**
     * Adds a new value to the specified HTTP header of this response.
     *
     * @param name  name of the HTTP header
     * @param value value to be added; if {@code null} existing value(s) will be removed
     */
    public void setHeader(String name, String value) {
        headers.add(name, value);
    }

    /**
     * Returns the HTTP headers of this response.
     *
     * @return HTTP headers of this response.
     */
    public MultivaluedMap<String, String> getHeaders() {
        return headers;
    }

    /**
     * Adds a cookie to this response.
     *
     * @param name  name of the cookie
     * @param value value of the cookie
     */
    public void addCookie(String name, String value) {
        cookies.put(name, value);
    }

    /**
     * Returns HTTP cookies of this response.
     *
     * @return HTTP cookies of this response
     */
    public Map<String, String> getCookies() {
        return cookies;
    }
}
