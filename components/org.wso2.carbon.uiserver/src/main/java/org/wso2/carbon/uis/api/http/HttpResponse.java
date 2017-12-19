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

import java.util.Collections;
import java.util.Map;

/**
 * Represents a HTTP response.
 *
 * @since 0.8.0
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
    private Map<String, String> headers;
    private Map<String, String> cookies;

    /**
     * Creates a new response.
     *
     * @param status      HTTP statusCode code of the response
     * @param content     content of the response
     * @param contentType MIME type of the response
     */
    public HttpResponse(int status, Object content, String contentType) {
        this(status, content, contentType, Collections.emptyMap(), Collections.emptyMap());
    }

    /**
     * Creates a new response.
     *
     * @param status      HTTP statusCode code of the response
     * @param content     content of the response
     * @param contentType MIME type of the response
     * @param headers     HTTP headers of the response
     * @param cookies     cookies of the response
     */
    public HttpResponse(int status, Object content, String contentType,
                        Map<String, String> headers, Map<String, String> cookies) {
        this.status = status;
        this.content = content;
        this.contentType = contentType;
        this.headers = Collections.unmodifiableMap(headers);
        this.cookies = Collections.unmodifiableMap(cookies);
    }

    /**
     * Returns the <a href="https://tools.ietf.org/html/rfc2616#section-10">HTTP statusCode code</a> of this response.
     *
     * @return HTTP statusCode code of this response
     */
    public int getStatus() {
        return status;
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
     * Returns the HTTP headers of this response.
     *
     * @return HTTP headers of this response.
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Returns HTTP cookies of this response.
     *
     * @return HTTP cookies of this response
     */
    public Map<String, String> getCookies() {
        return cookies;
    }

    @Override
    public String toString() {
        return "HttpResponse{status=" + status + ", content=" + content + ", contentType='" + contentType + "'}";
    }
}
