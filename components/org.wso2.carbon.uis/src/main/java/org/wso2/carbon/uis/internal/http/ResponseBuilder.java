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

import org.wso2.carbon.uis.api.http.HttpResponse;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder for {@link HttpResponse} class.
 *
 * @since 0.12.7
 */
public class ResponseBuilder {

    private int status;
    private Object content;
    private String contentType;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> cookies = new HashMap<>();

    /**
     * Sets the HTTP status code of the building response.
     *
     * @param status HTTP status code to be set
     * @return the updated response builder
     */
    public ResponseBuilder statusCode(int status) {
        this.status = status;
        return this;
    }

    /**
     * Sets the content of the building response.
     *
     * @param content content to be set
     * @return the updated response builder
     */
    public ResponseBuilder content(Object content) {
        this.content = content;
        return this;
    }

    /**
     * Sets the MIME type of the building response.
     *
     * @param contentType MIME type to be set
     * @return the updated response builder
     */
    public ResponseBuilder contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Sets a HTTP header of the building response.
     *
     * @param name  name of the HTTP header
     * @param value value of the HTTP header
     * @return the updated response builder
     */
    public ResponseBuilder header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    /**
     * Sets HTTP headers of the building response.
     *
     * @param headers HTTP headers (name, value pairs) to be set
     * @return the updated response builder
     */
    public ResponseBuilder headers(Map<String, String> headers) {
        headers.forEach(this::header);
        return this;
    }

    /**
     * Sets cookie of the building response.
     *
     * @param name  name of the cookie
     * @param value value of the cookie
     * @return the updated response builder
     */
    public ResponseBuilder cookie(String name, String value) {
        this.cookies.put(name, value);
        return this;
    }

    /**
     * Sets cookies of the building response.
     *
     * @param cookies cookies name, value pairs) to be set
     * @return the updated response builder
     */
    public ResponseBuilder cookies(Map<String, String> cookies) {
        cookies.forEach(this::cookie);
        return this;
    }

    /**
     * Build a HTTP response.
     *
     * @return response
     */
    public HttpResponse build() {
        return new HttpResponse(status, content, contentType, headers, cookies);
    }

    /**
     * Creates a new response builder with the specified HTTP status code.
     *
     * @param status HTTP status code
     * @return a new response builder
     */
    public static ResponseBuilder status(int status) {
        return new ResponseBuilder().statusCode(status);
    }

    /**
     * Creates a new response builder with {@link HttpResponse#STATUS_OK OK} status.
     *
     * @return a new response builder
     */
    public static ResponseBuilder ok() {
        return status(HttpResponse.STATUS_OK);
    }

    /**
     * Creates a new response builder with {@link HttpResponse#STATUS_OK OK} status.
     *
     * @param content content of the response
     * @return a new response builder
     */
    public static ResponseBuilder ok(String content) {
        return ok(content, HttpResponse.CONTENT_TYPE_TEXT_PLAIN);
    }

    /**
     * Creates a new response builder with {@link HttpResponse#STATUS_OK OK} status.
     *
     * @param content     content of the response
     * @param contentType MIME type of the response
     * @return a new response builder
     */
    public static ResponseBuilder ok(String content, String contentType) {
        return ok().content(content).contentType(contentType);
    }

    /**
     * Creates a new response builder with {@link HttpResponse#STATUS_OK OK} status.
     *
     * @param content     content of the response
     * @param contentType MIME type of the response
     * @return a new response builder
     */
    public static ResponseBuilder ok(File content, String contentType) {
        return ok().content(content).contentType(contentType);
    }

    /**
     * Creates a new response builder with {@link HttpResponse#STATUS_OK OK} status.
     *
     * @param content     content of the response
     * @param contentType MIME type of the response
     * @return a new response builder
     */
    public static ResponseBuilder ok(Path content, String contentType) {
        return ok(content.toFile(), contentType);
    }

    /**
     * Creates a new response builder with {@link HttpResponse#STATUS_OK OK} status.
     *
     * @param content     content of the response
     * @param contentType MIME type of the response
     * @return a new response builder
     */
    public static ResponseBuilder ok(InputStream content, String contentType) {
        return ok().content(content).contentType(contentType);
    }

    /**
     * Creates a new response builder with {@link HttpResponse#STATUS_BAD_REQUEST BAD_REQUEST} status.
     *
     * @return a new response builder
     */
    public static ResponseBuilder badRequest() {
        return status(HttpResponse.STATUS_BAD_REQUEST);
    }

    /**
     * Creates a new response builder with {@link HttpResponse#STATUS_BAD_REQUEST BAD_REQUEST} status.
     *
     * @param content content of the response
     * @return a new response builder
     */
    public static ResponseBuilder badRequest(String content) {
        return badRequest().content(content).contentType(HttpResponse.CONTENT_TYPE_TEXT_PLAIN);
    }

    /**
     * Creates a new response builder with {@link HttpResponse#STATUS_NOT_FOUND NOT_FOUND} status.
     *
     * @return a new response builder
     */
    public static ResponseBuilder notFound() {
        return status(HttpResponse.STATUS_NOT_FOUND);
    }

    /**
     * Creates a new response builder with {@link HttpResponse#STATUS_NOT_FOUND NOT_FOUND} status.
     *
     * @param content content of the response
     * @return a new response builder
     */
    public static ResponseBuilder notFound(String content) {
        return notFound().content(content).contentType(HttpResponse.CONTENT_TYPE_TEXT_PLAIN);
    }

    /**
     * Creates a new response builder with {@link HttpResponse#STATUS_INTERNAL_SERVER_ERROR INTERNAL_SERVER_ERROR}
     * status.
     *
     * @return a new response builder
     */
    public static ResponseBuilder serverError() {
        return status(HttpResponse.STATUS_INTERNAL_SERVER_ERROR);
    }

    /**
     * Creates a new response builder with {@link HttpResponse#STATUS_INTERNAL_SERVER_ERROR INTERNAL_SERVER_ERROR}
     * status.
     *
     * @param content content of the response
     * @return a new response builder
     */
    public static ResponseBuilder serverError(String content) {
        return serverError().content(content).contentType(HttpResponse.CONTENT_TYPE_TEXT_PLAIN);
    }
}
