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

package org.wso2.carbon.uiserver.api.http;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for {@link HttpResponse} class.
 *
 * @since 0.13.0
 */
public class HttpResponseTest {

    @Test
    public void test() {
        HttpResponse response = new HttpResponse(HttpResponse.STATUS_OK, "some-content",
                                                 HttpResponse.CONTENT_TYPE_TEXT_PLAIN);

        Assert.assertEquals(response.getStatus(), HttpResponse.STATUS_OK);
        Assert.assertEquals(response.getContent(), "some-content");
        Assert.assertEquals(response.getContentType(), HttpResponse.CONTENT_TYPE_TEXT_PLAIN);
        Assert.assertTrue(response.getHeaders().isEmpty());
        Assert.assertTrue(response.getCookies().isEmpty());
        Assert.assertNotNull(response.toString());
    }
}
