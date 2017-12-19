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

package org.wso2.carbon.uiserver.internal.http;

import org.wso2.carbon.uiserver.api.App;
import org.wso2.carbon.uiserver.api.exception.PageNotFoundException;
import org.wso2.carbon.uiserver.api.exception.PageRedirectException;
import org.wso2.carbon.uiserver.api.exception.RenderingException;
import org.wso2.carbon.uiserver.api.http.HttpRequest;
import org.wso2.carbon.uiserver.api.http.HttpResponse;

import static org.wso2.carbon.uiserver.api.http.HttpResponse.CONTENT_TYPE_TEXT_HTML;
import static org.wso2.carbon.uiserver.api.http.HttpResponse.HEADER_LOCATION;

/**
 * Dispatcher for HTTP request for pages.
 *
 * @since 0.13.4
 */
public class PageRequestDispatcher {

    private final App app;

    /**
     * Creates a new request dispatcher.
     *
     * @param app web app to be served
     */
    public PageRequestDispatcher(App app) {
        this.app = app;
    }

    /**
     * Serves to the supplied HTTP request and returns a HTTP response.
     *
     * @param request HTTP request to be served
     * @return a HTTP response that carries the result
     */
    public HttpResponse serve(HttpRequest request) {
        try {
            String html = app.renderPage(request);
            return ResponseBuilder.ok(html, CONTENT_TYPE_TEXT_HTML)
                    .headers(app.getConfiguration().getResponseHeaders().forPages())
                    .build();
        } catch (RenderingException e) {
            return ResponseBuilder
                    .serverError("An error occurred when rendering page '" + request.getUriWithoutContextPath() + "'.")
                    .build();
        } catch (PageNotFoundException e) {
            return ResponseBuilder.notFound("Page '" + request.getUriWithoutContextPath() + "' does not exists.")
                    .build();
        } catch (PageRedirectException e) {
            return ResponseBuilder.status(e.getHttpStatusCode())
                    .header(HEADER_LOCATION, e.getRedirectUrl())
                    .build();
        }
    }
}
