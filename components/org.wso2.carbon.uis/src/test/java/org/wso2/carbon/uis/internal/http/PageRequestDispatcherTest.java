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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uis.api.App;
import org.wso2.carbon.uis.api.Configuration;
import org.wso2.carbon.uis.api.exception.PageNotFoundException;
import org.wso2.carbon.uis.api.exception.PageRedirectException;
import org.wso2.carbon.uis.api.exception.RenderingException;
import org.wso2.carbon.uis.api.http.HttpRequest;
import org.wso2.carbon.uis.api.http.HttpResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link PageRequestDispatcher} class.
 *
 * @since 0.13.4
 */
public class PageRequestDispatcherTest {

    @Test
    public void testServe() {
        App app = mock(App.class);
        when(app.renderPage(any())).thenReturn("<p>some html</p>");
        when(app.getConfiguration()).thenReturn(Configuration.DEFAULT_CONFIGURATION);

        HttpResponse response = new PageRequestDispatcher(app).serve(mock(HttpRequest.class));
        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_OK);
        Assert.assertEquals(response.getContent(), "<p>some html</p>");
        Assert.assertEquals(response.getContentType(), HttpResponse.CONTENT_TYPE_TEXT_HTML);
    }

    @Test
    public void testServeWithRenderingException() {
        App app = mock(App.class);
        when(app.renderPage(any())).thenThrow(RenderingException.class);

        HttpResponse response = new PageRequestDispatcher(app).serve(mock(HttpRequest.class));
        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testServeWithPageNotFoundException() {
        App app = mock(App.class);
        when(app.renderPage(any())).thenThrow(PageNotFoundException.class);

        HttpResponse response = new PageRequestDispatcher(app).serve(mock(HttpRequest.class));
        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_NOT_FOUND);
    }

    @Test
    public void testServeWithPageRedirectException() {
        App app = mock(App.class);
        when(app.renderPage(any())).thenThrow(new PageRedirectException("redirect/url"));

        HttpResponse response = new PageRequestDispatcher(app).serve(mock(HttpRequest.class));
        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_FOUND);
        Assert.assertEquals(response.getHeaders().get(HttpResponse.HEADER_LOCATION), "redirect/url");
    }
}
