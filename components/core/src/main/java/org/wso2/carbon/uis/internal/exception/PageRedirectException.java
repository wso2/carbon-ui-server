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

package org.wso2.carbon.uis.internal.exception;

import org.wso2.carbon.uis.api.http.HttpResponse;

/**
 * Indicates a page redirection.
 *
 * @since 0.8.0
 */
public class PageRedirectException extends HttpErrorException {

    private final String redirectUrl;

    /**
     * Constructs a new exception with the specified redirect URL.
     *
     * @param redirectUrl redirection URI or URL
     */
    public PageRedirectException(String redirectUrl) {
        super(HttpResponse.STATUS_FOUND, "Redirecting to '" + redirectUrl + "'.");
        this.redirectUrl = redirectUrl;
    }

    /**
     * Constructs a new exception with the specified redirect URL.
     *
     * @param redirectUrl redirection URI or URL
     * @param cause       the cause of the exception
     */
    public PageRedirectException(String redirectUrl, Throwable cause) {
        super(HttpResponse.STATUS_FOUND, "Redirecting to '" + redirectUrl + "'.", cause);
        this.redirectUrl = redirectUrl;
    }

    /**
     * Returns the redirection URL of this exception.
     *
     * @return the redirection URL
     */
    public String getRedirectUrl() {
        return redirectUrl;
    }
}
