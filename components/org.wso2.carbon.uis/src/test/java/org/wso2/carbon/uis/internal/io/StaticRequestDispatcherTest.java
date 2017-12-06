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

package org.wso2.carbon.uis.internal.io;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.uis.api.App;
import org.wso2.carbon.uis.api.http.HttpRequest;
import org.wso2.carbon.uis.api.http.HttpResponse;
import org.wso2.carbon.uis.internal.deployment.AppCreator;
import org.wso2.carbon.uis.internal.io.reference.ArtifactAppReference;

import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link StaticRequestDispatcher} class.
 *
 * @since 0.12.5
 */
public class StaticRequestDispatcherTest {

    @Test
    public void testServeDefaultFavicon() {
        final HttpResponse response = new StaticRequestDispatcher().serveDefaultFavicon(null);

        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_OK);
        Assert.assertEquals(response.getContentType(), HttpResponse.CONTENT_TYPE_IMAGE_PNG);
        Assert.assertNotNull(response.getContent());
    }

    @DataProvider
    public Object[][] invalidUris() {
        return new Object[][]{
                {createRequest("/public/")},
                {createRequest("/public/app/")},
                {createRequest("/public/extensions/")},
                {createRequest("/public/extensions/foo")},
                {createRequest("/public/themes/")},
                {createRequest("/public/themes/bar")}
        };
    }

    @Test(dataProvider = "invalidUris")
    public void testServeWithInvalidUri(HttpRequest request) {
        final HttpResponse response = new StaticRequestDispatcher().serve(creatApp(), request);

        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_BAD_REQUEST);
        Assert.assertEquals(response.getContentType(), HttpResponse.CONTENT_TYPE_TEXT_PLAIN);
        Assert.assertNotNull(response.getContent());
    }

    private static HttpRequest createRequest(String uriWithoutContextPath) {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getUriWithoutContextPath()).thenReturn(uriWithoutContextPath);
        return request;
    }

    private static App creatApp() {
        return AppCreator.createApp(new ArtifactAppReference(Paths.get("src/test/resources/apps/full-app/")), "/test");
    }
}
