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
import org.wso2.carbon.uis.api.exception.UISRuntimeException;
import org.wso2.carbon.uis.api.http.HttpRequest;
import org.wso2.carbon.uis.api.http.HttpResponse;
import org.wso2.carbon.uis.internal.io.http.StaticRequestDispatcher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link RequestDispatcher} class.
 *
 * @since 0.13.4
 */
public class RequestDispatcherTest {

    @Test
    public void testServeInvalidRequest() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.isValid()).thenReturn(false);

        HttpResponse response = new RequestDispatcher(null).serve(request);
        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_BAD_REQUEST);
        Assert.assertNotNull(response.getContent());
    }

    @Test
    public void testServeFaviconRequest() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.isValid()).thenReturn(true);
        when(request.isDefaultFaviconRequest()).thenReturn(true);
        StaticRequestDispatcher staticRequestDispatcher = mock(StaticRequestDispatcher.class);

        new RequestDispatcher(null, staticRequestDispatcher).serve(request);
        verify(staticRequestDispatcher).serveDefaultFavicon(request);
    }

    @Test
    public void testServeStaticRequest() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.isValid()).thenReturn(true);
        when(request.isDefaultFaviconRequest()).thenReturn(false);
        when(request.isStaticResourceRequest()).thenReturn(true);
        StaticRequestDispatcher staticRequestDispatcher = mock(StaticRequestDispatcher.class);

        new RequestDispatcher(null, staticRequestDispatcher).serve(request);
        verify(staticRequestDispatcher).serve(request);
    }

    @Test
    public void testServePageRequest() {
        HttpRequest request = createPageRequest();
        PageRequestDispatcher pageRequestDispatcher = mock(PageRequestDispatcher.class);

        new RequestDispatcher(pageRequestDispatcher, null).serve(request);
        verify(pageRequestDispatcher).serve(request);
    }

    @Test
    public void testServeWhenUISRuntimeException() {
        HttpRequest request = createPageRequest();
        PageRequestDispatcher pageRequestDispatcher = mock(PageRequestDispatcher.class);
        when(pageRequestDispatcher.serve(any())).thenThrow(UISRuntimeException.class);

        HttpResponse response = new RequestDispatcher(pageRequestDispatcher, null).serve(request);
        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_INTERNAL_SERVER_ERROR);
        Assert.assertNotNull(response.getContent());
    }

    @Test
    public void testServeWhenException() {
        HttpRequest request = createPageRequest();
        PageRequestDispatcher pageRequestDispatcher = mock(PageRequestDispatcher.class);
        when(pageRequestDispatcher.serve(any())).thenThrow(Exception.class);

        HttpResponse response = new RequestDispatcher(pageRequestDispatcher, null).serve(request);
        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_INTERNAL_SERVER_ERROR);
        Assert.assertNotNull(response.getContent());
    }

    private static HttpRequest createPageRequest() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.isValid()).thenReturn(true);
        when(request.isDefaultFaviconRequest()).thenReturn(false);
        when(request.isStaticResourceRequest()).thenReturn(false);
        return request;
    }
}
