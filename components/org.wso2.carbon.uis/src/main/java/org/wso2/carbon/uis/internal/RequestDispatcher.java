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

package org.wso2.carbon.uis.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uis.api.App;
import org.wso2.carbon.uis.api.exception.UISRuntimeException;
import org.wso2.carbon.uis.api.http.HttpRequest;
import org.wso2.carbon.uis.api.http.HttpResponse;
import org.wso2.carbon.uis.internal.exception.HttpErrorException;
import org.wso2.carbon.uis.internal.exception.PageRedirectException;
import org.wso2.carbon.uis.internal.http.ResponseBuilder;
import org.wso2.carbon.uis.internal.io.StaticResolver;

import static org.wso2.carbon.uis.api.http.HttpResponse.CONTENT_TYPE_TEXT_HTML;
import static org.wso2.carbon.uis.api.http.HttpResponse.CONTENT_TYPE_TEXT_PLAIN;
import static org.wso2.carbon.uis.api.http.HttpResponse.HEADER_LOCATION;
import static org.wso2.carbon.uis.api.http.HttpResponse.STATUS_BAD_REQUEST;
import static org.wso2.carbon.uis.api.http.HttpResponse.STATUS_INTERNAL_SERVER_ERROR;

/**
 * Dispatches HTTP requests.
 *
 * @since 0.8.0
 */
public class RequestDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestDispatcher.class);

    private final App app;
    private final StaticResolver staticResolver;

    /**
     * Creates a new request dispatcher.
     *
     * @param app web app to be served
     */
    public RequestDispatcher(App app) {
        this(app, new StaticResolver());
    }

    RequestDispatcher(App app, StaticResolver staticResolver) {
        this.app = app;
        this.staticResolver = staticResolver;
    }

    /**
     * Serves the specified HTTP request.
     *
     * @param request HTTP request to be served
     * @return HTTP response
     */
    public HttpResponse serve(HttpRequest request) {
        if (!request.isValid()) {
            return serveDefaultErrorPage(STATUS_BAD_REQUEST, "Invalid URI '" + request.getUri() + "'.");
        }
        if (request.isDefaultFaviconRequest()) {
            return serveDefaultFavicon(request);
        }

        return serve(app, request);
    }

    private HttpResponse serve(App app, HttpRequest request) {
        try {
            if (request.isStaticResourceRequest()) {
                return staticResolver.serve(app, request);
            } else {
                return servePage(app, request);
            }
        } catch (PageRedirectException e) {
            return ResponseBuilder.status(e.getHttpStatusCode())
                    .header(HEADER_LOCATION, e.getRedirectUrl())
                    .build();
        } catch (HttpErrorException e) {
            return serveDefaultErrorPage(e.getHttpStatusCode(), e.getMessage());
        } catch (UISRuntimeException e) {
            String msg = "A server error occurred while serving for request '" + request + "'.";
            LOGGER.error(msg, e);
            return serveDefaultErrorPage(STATUS_INTERNAL_SERVER_ERROR, msg);
        } catch (Exception e) {
            String msg = "An unexpected error occurred while serving for request '" + request + "'.";
            LOGGER.error(msg, e);
            return serveDefaultErrorPage(STATUS_INTERNAL_SERVER_ERROR, msg);
        }
    }

    private HttpResponse servePage(App app, HttpRequest request) {
        try {
            String html = app.renderPage(request);
            return ResponseBuilder.ok(html, CONTENT_TYPE_TEXT_HTML)
                    .headers(app.getConfiguration().getResponseHeaders().forPages())
                    .build();
        } catch (UISRuntimeException e) {
            throw e;
        } catch (Exception e) {
            // May be an UISRuntimeException cause this 'e' Exception. Let's unwrap 'e' and find out.
            Throwable th = e;
            while ((th = th.getCause()) != null) {
                if (th instanceof UISRuntimeException) {
                    // Cause of 'e' is an UISRuntimeException. Throw 'th' so that we can handle it properly.
                    throw (UISRuntimeException) th;
                }
            }
            // Cause of 'e' is not an UISRuntimeException.
            throw e;
        }
    }

    private HttpResponse serveDefaultErrorPage(int httpStatusCode, String content) {
        return ResponseBuilder.status(httpStatusCode)
                .content(content)
                .contentType(CONTENT_TYPE_TEXT_PLAIN)
                .build();
    }

    private HttpResponse serveDefaultFavicon(HttpRequest request) {
        return staticResolver.serveDefaultFavicon(request);
    }
}
