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

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uis.api.http.HttpResponse;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

/**
 * Test cases for {@link ResponseBuilder} class.
 *
 * @since 0.12.7
 */
public class ResponseBuilderTest {

    @Test
    public void testHeaders() {
        Map<String, String> headers = Collections.singletonMap("some-header", "some-value");
        HttpResponse response = new ResponseBuilder().statusCode(HttpResponse.STATUS_OK)
                .headers(headers)
                .build();

        Assert.assertEquals(response.getHeaders(), headers);
    }

    @Test
    public void testCookies() {
        Map<String, String> cookies = Collections.singletonMap("some-cookie", "some-value");
        HttpResponse response = new ResponseBuilder().statusCode(HttpResponse.STATUS_OK)
                .cookies(cookies)
                .build();

        Assert.assertEquals(response.getCookies(), cookies);
    }

    @Test
    public void testOk() {
        HttpResponse response = ResponseBuilder.ok().build();
        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_OK);
    }

    @Test
    public void testOkString() {
        final String content = "<p>everything ok</p>";
        HttpResponse response = ResponseBuilder.ok(content, HttpResponse.CONTENT_TYPE_TEXT_HTML).build();

        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_OK);
        Assert.assertEquals(response.getContent(), content);
        Assert.assertEquals(response.getContentType(), HttpResponse.CONTENT_TYPE_TEXT_HTML);
    }

    @Test
    public void testOkPath() {
        final Path content = Paths.get("src/resources/apps/full-app/public/css/styles.css");
        HttpResponse response = ResponseBuilder.ok(content, HttpResponse.CONTENT_TYPE_TEXT_PLAIN).build();

        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_OK);
        Assert.assertEquals(response.getContent(), content.toFile());
        Assert.assertEquals(response.getContentType(), HttpResponse.CONTENT_TYPE_TEXT_PLAIN);
    }

    @Test
    public void testOkInputStream() {
        final InputStream content = Mockito.mock(InputStream.class);
        HttpResponse response = ResponseBuilder.ok(content, HttpResponse.CONTENT_TYPE_TEXT_PLAIN).build();

        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_OK);
        Assert.assertEquals(response.getContent(), content);
        Assert.assertEquals(response.getContentType(), HttpResponse.CONTENT_TYPE_TEXT_PLAIN);
    }

    @Test
    public void testBadRequest() {
        final String content = "some bad request";
        HttpResponse response = ResponseBuilder.badRequest(content).build();

        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_BAD_REQUEST);
        Assert.assertEquals(response.getContent(), content);
        Assert.assertEquals(response.getContentType(), HttpResponse.CONTENT_TYPE_TEXT_PLAIN);
    }

    @Test
    public void testNotFound() {
        final String content = "some resource/page not found";
        HttpResponse response = ResponseBuilder.notFound(content).build();

        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_NOT_FOUND);
        Assert.assertEquals(response.getContent(), content);
        Assert.assertEquals(response.getContentType(), HttpResponse.CONTENT_TYPE_TEXT_PLAIN);
    }

    @Test
    public void testServerError() {
        final String content = "some internal server error";
        HttpResponse response = ResponseBuilder.serverError(content).build();

        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_INTERNAL_SERVER_ERROR);
        Assert.assertEquals(response.getContent(), content);
        Assert.assertEquals(response.getContentType(), HttpResponse.CONTENT_TYPE_TEXT_PLAIN);
    }
}
