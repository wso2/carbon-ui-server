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

package org.wso2.carbon.uis.internal.io.http;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.uis.api.App;
import org.wso2.carbon.uis.api.http.HttpRequest;
import org.wso2.carbon.uis.api.http.HttpResponse;
import org.wso2.carbon.uis.internal.deployment.AppCreator;
import org.wso2.carbon.uis.internal.io.reference.ArtifactAppReference;

import java.nio.file.Paths;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.uis.api.http.HttpResponse.HEADER_LAST_MODIFIED;

import static java.util.Collections.singletonMap;

/**
 * Test cases for {@link StaticRequestDispatcher} class.
 *
 * @since 0.12.5
 */
public class StaticRequestDispatcherTest {

    @Test
    public void testServeDefaultFavicon() {
        final HttpResponse response = new StaticRequestDispatcher(null).serveDefaultFavicon(null);

        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_OK);
        Assert.assertEquals(response.getContentType(), HttpResponse.CONTENT_TYPE_IMAGE_PNG);
        Assert.assertNotNull(response.getContent());
    }

    @DataProvider
    public Object[][] invalidRequests() {
        return new Object[][]{
                {createRequest("/public/")},
                {createRequest("/public/app/")},
                {createRequest("/public/extensions/")},
                {createRequest("/public/extensions/foo")},
                {createRequest("/public/extensions/foo/")},
                {createRequest("/public/themes/")},
                {createRequest("/public/themes/bar")},
                {createRequest("/public/themes/bar/")}
        };
    }

    @Test(dataProvider = "invalidRequests")
    public void testServeInvalidRequest(HttpRequest request) {
        HttpResponse response = new StaticRequestDispatcher(creatApp()).serve(request);

        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_BAD_REQUEST);
        Assert.assertNotNull(response.getContent());
    }

    @Test
    public void testServeAppStaticResourceRequest() {
        App app = creatApp();
        HttpRequest request = createRequest("/public/app/css/styles.css");

        HttpResponse response = new StaticRequestDispatcher(app).serve(request);
        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_OK);
        Assert.assertEquals(response.getContentType(), "text/css");
        app.getConfiguration().getResponseHeaders().forStaticResources().forEach((header, value) -> {
            Assert.assertEquals(response.getHeaders().get(header), value,
                                "Value '" + value + "' of configured response header '" + header +
                                "' for static resources does not exists in the response.");
        });
    }

    @Test
    public void testServeExtensionStaticResourceRequest() {
        HttpRequest request = createRequest("/public/extensions/widgets/line-chart/css/styles.css");

        HttpResponse response = new StaticRequestDispatcher(creatApp()).serve(request);
        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_OK);
        Assert.assertEquals(response.getContentType(), "text/css");
    }

    @Test
    public void testServeNotFoundExtensionStaticResourceRequest() {
        HttpRequest request = createRequest("/public/extensions/foo/bar/css/styles.css");

        HttpResponse response = new StaticRequestDispatcher(creatApp()).serve(request);
        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_NOT_FOUND);
        Assert.assertNotNull(response.getContent());
    }

    @Test
    public void testServeThemeStaticResourceRequest() {
        HttpRequest request = createRequest("/public/themes/light/css/styles.css");

        HttpResponse response = new StaticRequestDispatcher(creatApp()).serve(request);
        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_OK);
        Assert.assertEquals(response.getContentType(), "text/css");
    }

    @Test
    public void testServeNotFoundThemeStaticResourceRequest() {
        HttpRequest request = createRequest("/public/themes/foo/css/styles.css");

        HttpResponse response = new StaticRequestDispatcher(creatApp()).serve(request);
        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_NOT_FOUND);
        Assert.assertNotNull(response.getContent());
    }

    @Test
    public void testServeWhenIfModifiedSinceDateHeaderPresent() {
        HttpRequest request = createRequest("/public/app/css/styles.css");
        when(request.getHeaders()).thenReturn(
                singletonMap("If-Modified-Since", "Sat, 27 May 2017 10:20:30 GMT"));

        HttpResponse response = new StaticRequestDispatcher(creatApp()).serve(request);
        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_OK);
        Assert.assertEquals(response.getContentType(), "text/css");
        Assert.assertNotNull(response.getHeaders().get(HEADER_LAST_MODIFIED));
    }

    @Test
    public void testServeWhenIfModifiedSinceDateHeaderInvalid() {
        HttpRequest request = createRequest("/public/app/css/styles.css");
        when(request.getHeaders()).thenReturn(singletonMap("If-Modified-Since", "foo bar"));

        HttpResponse response = new StaticRequestDispatcher(creatApp()).serve(request);
        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_OK);
        Assert.assertEquals(response.getContentType(), "text/css");
        Assert.assertNotNull(response.getHeaders().get(HEADER_LAST_MODIFIED));
    }

    @Test
    public void testServeWhenResourceNotModified() {
        StaticRequestDispatcher staticRequestDispatcher = new StaticRequestDispatcher(creatApp());

        // first serve
        HttpRequest previousRequest = createRequest("/public/app/css/styles.css");
        HttpResponse previousResponse = staticRequestDispatcher.serve(previousRequest);

        // second serve
        HttpRequest request = createRequest("/public/app/css/styles.css");
        when(request.getHeaders()).thenReturn(singletonMap("If-Modified-Since",
                                                           previousResponse.getHeaders().get(HEADER_LAST_MODIFIED)));
        HttpResponse response = staticRequestDispatcher.serve(request);
        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_NOT_MODIFIED);
    }

    private static HttpRequest createRequest(String uriWithoutContextPath) {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getUriWithoutContextPath()).thenReturn(uriWithoutContextPath);
        when(request.getHeaders()).thenReturn(Collections.emptyMap());
        when(request.isAppStaticResourceRequest()).thenCallRealMethod();
        when(request.isExtensionStaticResourceRequest()).thenCallRealMethod();
        when(request.isThemeStaticResourceRequest()).thenCallRealMethod();
        return request;
    }

    private static App creatApp() {
        return AppCreator.createApp(new ArtifactAppReference(Paths.get("src/test/resources/apps/full-app/")), "/test");
    }
}
