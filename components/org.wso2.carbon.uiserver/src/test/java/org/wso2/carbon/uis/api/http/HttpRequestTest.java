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

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link HttpRequest} interface.
 *
 * @since 0.14.0
 */
public class HttpRequestTest {

    @Test
    public void testGetScheme() {
        HttpRequest request = createRequest("/foo/bar");

        when(request.isSecure()).thenReturn(false);
        Assert.assertEquals(request.getScheme(), "http");

        when(request.isSecure()).thenReturn(true);
        Assert.assertEquals(request.getScheme(), "https");
    }

    @DataProvider
    public Object[][] invalidRequests() {
        return new Object[][]{
                {createRequest(null)},
                {createRequest("")},
                {createRequest(" ")},
                {createRequest("foo")},
                {createRequest("foo/bar")},
                {createRequest("/foo/../bar")},
                {createRequest("/foo//bar")},
                {createRequest("////foo")},
                {createRequest("/../../foo/bar")}
        };
    }

    @Test(dataProvider = "invalidRequests")
    public void testIsValidWithInvalidUris(HttpRequest request) {
        Assert.assertFalse(request.isValid(), "URI '" + request.getUri() + "' is invalid.");
    }

    @DataProvider
    public Object[][] validRequests() {
        return new Object[][]{
                {createRequest("/foo")},
                {createRequest("/foo/bar")},
                {createRequest("/foo/bar/barz")}
        };
    }

    @Test(dataProvider = "validRequests")
    public void testIsValidWithValidUris(HttpRequest request) {
        Assert.assertTrue(request.isValid(), "URI '" + request.getUri() + "' is valid.");
    }

    @DataProvider
    public Object[][] staticResourceRequests() {
        return new Object[][]{
                {createRequest("/app/public/")},
                {createRequest("/app/public/app")},
                {createRequest("/app/public/app/")},
                {createRequest("/app/public/app/css/styles.css")},
                {createRequest("/app/public/extensions")},
                {createRequest("/app/public/extensions/")},
                {createRequest("/app/public/extensions/widgets")},
                {createRequest("/app/public/extensions/widgets/")},
                {createRequest("/app/public/extensions/widgets/line-chart")},
                {createRequest("/app/public/extensions/widgets/line-chart/")},
                {createRequest("/app/public/extensions/widgets/line-chart/css/styles.css")},
                {createRequest("/app/public/themes")},
                {createRequest("/app/public/themes/")},
                {createRequest("/app/public/themes/dark")},
                {createRequest("/app/public/themes/dark/")},
                {createRequest("/app/public/themes/dark/css/styles.css")}
        };
    }

    @Test(dataProvider = "staticResourceRequests")
    public void testIsStaticResourceRequest(HttpRequest request) {
        Assert.assertTrue(request.isStaticResourceRequest(),
                          "URI '" + request.getUri() + "' is a static resource request.");
    }

    @DataProvider
    public Object[][] appStaticResourceRequest() {
        return new Object[][]{
                {createRequest("/app/public/app/")},
                {createRequest("/app/public/app/css/styles.css")}
        };
    }

    @Test(dataProvider = "appStaticResourceRequest")
    public void testIsAppStaticResourceRequest(HttpRequest request) {
        Assert.assertTrue(request.isAppStaticResourceRequest(),
                          "URI '" + request.getUri() + "' is an app static resource request");
    }

    @DataProvider
    public Object[][] extensionStaticResourceRequest() {
        return new Object[][]{
                {createRequest("/app/public/extensions/")},
                {createRequest("/app/public/extensions/widgets")},
                {createRequest("/app/public/extensions/widgets/")},
                {createRequest("/app/public/extensions/widgets/line-chart")},
                {createRequest("/app/public/extensions/widgets/line-chart/")},
                {createRequest("/app/public/extensions/widgets/line-chart/css/styles.css")}
        };
    }

    @Test(dataProvider = "extensionStaticResourceRequest")
    public void testIsExtensionStaticResourceRequest(HttpRequest request) {
        Assert.assertTrue(request.isExtensionStaticResourceRequest(),
                          "URI '" + request.getUri() + "' is an extension static resource request");
    }

    @DataProvider
    public Object[][] themeStaticResourceRequest() {
        return new Object[][]{
                {createRequest("/app/public/themes/")},
                {createRequest("/app/public/themes/dark")},
                {createRequest("/app/public/themes/dark/")},
                {createRequest("/app/public/themes/dark/css/styles.css")}
        };
    }

    @Test(dataProvider = "themeStaticResourceRequest")
    public void testIsThemeStaticResourceRequest(HttpRequest request) {
        Assert.assertTrue(request.isThemeStaticResourceRequest(),
                          "URI '" + request.getUri() + "' is a theme static resource request");
    }

    @Test
    public void testIsDefaultFaviconRequest() {
        HttpRequest request1 = createRequest("/favicon.ico");
        Assert.assertTrue(request1.isDefaultFaviconRequest(),
                          "URI '" + request1.getUri() + "' is a default favicon request.");

        HttpRequest request2 = createRequest("/favicon.foo");
        Assert.assertFalse(request2.isDefaultFaviconRequest(),
                           "URI '" + request2.getUri() + "' is NOT a default favicon request.");
    }

    private static HttpRequest createRequest(String uri) {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getUri()).thenReturn(uri);
        when(request.getContextPath())
                .thenReturn((uri == null) ? "" : HttpRequest.getContextPath(uri));
        when(request.getUriWithoutContextPath())
                .thenReturn((uri == null) ? "" : HttpRequest.getUriWithoutContextPath(uri));

        when(request.getScheme()).thenCallRealMethod();
        when(request.isValid()).thenCallRealMethod();
        when(request.isStaticResourceRequest()).thenCallRealMethod();
        when(request.isAppStaticResourceRequest()).thenCallRealMethod();
        when(request.isExtensionStaticResourceRequest()).thenCallRealMethod();
        when(request.isThemeStaticResourceRequest()).thenCallRealMethod();
        when(request.isDefaultFaviconRequest()).thenCallRealMethod();

        return request;
    }
}
