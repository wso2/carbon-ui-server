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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uis.api.App;
import org.wso2.carbon.uis.api.exception.HttpErrorException;
import org.wso2.carbon.uis.api.exception.PageRedirectException;
import org.wso2.carbon.uis.api.exception.UISRuntimeException;
import org.wso2.carbon.uis.api.http.HttpRequest;
import org.wso2.carbon.uis.api.http.HttpResponse;

import static org.wso2.carbon.uis.api.http.HttpResponse.CONTENT_TYPE_TEXT_HTML;
import static org.wso2.carbon.uis.api.http.HttpResponse.CONTENT_TYPE_TEXT_PLAIN;
import static org.wso2.carbon.uis.api.http.HttpResponse.HEADER_LOCATION;
import static org.wso2.carbon.uis.api.http.HttpResponse.STATUS_INTERNAL_SERVER_ERROR;

public class PageRequestDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageRequestDispatcher.class);

    private final App app;

    public PageRequestDispatcher(App app) {
        this.app = app;
    }

    public HttpResponse server(HttpRequest request) {
        try {
            String html = app.renderPage(request);
            return ResponseBuilder.ok(html, CONTENT_TYPE_TEXT_HTML)
                    .headers(app.getConfiguration().getResponseHeaders().forPages())
                    .build();
        } catch (PageRedirectException e) {
            return ResponseBuilder.status(e.getHttpStatusCode())
                    .header(HEADER_LOCATION, e.getRedirectUrl())
                    .build();
        } catch (HttpErrorException e) {
            return serveDefaultErrorPage(e.getHttpStatusCode(), e.getMessage());
        } catch (UISRuntimeException e) {
            LOGGER.error("Cannot serve request {} due to a runtime exception in the server.", request, e);
            return serveDefaultErrorPage(STATUS_INTERNAL_SERVER_ERROR, "Internal server error.");
        }
    }

    private static HttpResponse serveDefaultErrorPage(int httpStatusCode, String content) {
        return ResponseBuilder.status(httpStatusCode)
                .content(content)
                .contentType(CONTENT_TYPE_TEXT_PLAIN)
                .build();
    }
}
