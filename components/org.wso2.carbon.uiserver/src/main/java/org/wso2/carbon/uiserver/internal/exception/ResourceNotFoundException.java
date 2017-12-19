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

package org.wso2.carbon.uiserver.internal.exception;

import org.wso2.carbon.uiserver.api.exception.HttpErrorException;
import org.wso2.carbon.uiserver.api.http.HttpResponse;

/**
 * Indicates a resource not found error.
 *
 * @since 0.8.0
 */
public class ResourceNotFoundException extends HttpErrorException {

    /**
     * Constructs a new exception with {@code null} as its detail message.
     */
    public ResourceNotFoundException() {
        super(HttpResponse.STATUS_NOT_FOUND);
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message detail message
     */
    public ResourceNotFoundException(String message) {
        super(HttpResponse.STATUS_NOT_FOUND, message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message details message
     * @param cause   the cause of the exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(HttpResponse.STATUS_NOT_FOUND, message, cause);
    }
}
