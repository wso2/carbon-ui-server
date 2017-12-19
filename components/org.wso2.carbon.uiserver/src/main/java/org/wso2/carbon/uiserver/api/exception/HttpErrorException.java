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

package org.wso2.carbon.uiserver.api.exception;

/**
 * Indicates a HTTP error.
 *
 * @since 0.8.0
 */
public class HttpErrorException extends UiServerRuntimeException {

    private final int httpStatusCode;

    /**
     * Constructs a new exception with the specified HTTP status code and {@code null} as its detail message.
     *
     * @param httpStatusCode HTTP error status code
     */
    public HttpErrorException(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * Constructs a new exception with the specified HTTP status code and detail message.
     *
     * @param httpStatusCode HTTP error status code
     * @param message        detail message
     */
    public HttpErrorException(int httpStatusCode, String message) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * Constructs a new exception with the specified HTTP status code, detail message, and cause.
     *
     * @param httpStatusCode HTTP error status code
     * @param message        detail message
     * @param cause          the cause of the exception
     */
    public HttpErrorException(int httpStatusCode, String message, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
    }

    /**
     * Returns the HTTP status error code of this HTTP exception.
     *
     * @return HTTP status code of the HTTP exception
     */
    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }
}
