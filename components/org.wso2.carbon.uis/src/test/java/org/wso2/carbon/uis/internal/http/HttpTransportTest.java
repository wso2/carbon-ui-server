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

/**
 * Test cases for {@link HttpTransport} class.
 *
 * @since 0.15.0
 */
public class HttpTransportTest {

    @Test
    public void test() {
        final String id = "some-id";
        final String scheme = "http";
        final String host = "localhost";
        final int port = 9292;

        HttpTransport httpTransport = new HttpTransport(id, scheme, host, port);
        Assert.assertEquals(httpTransport.getId(), id);
        Assert.assertEquals(httpTransport.getScheme(), scheme);
        Assert.assertEquals(httpTransport.getHost(), host);
        Assert.assertEquals(httpTransport.getPort(), port);
    }

    @Test
    public void testIsSecured() {
        Assert.assertFalse(createHttpTransport("any", "http").isSecured());
        Assert.assertTrue(createHttpTransport("any-thing", "HTTPS").isSecured());
    }

    @Test
    public void testEquals() {
        Assert.assertNotEquals(null, createHttpTransport("foo", "http"));
        Assert.assertNotEquals(new Object(), createHttpTransport("foo", "http"));
        Assert.assertNotEquals(createHttpTransport("foo", "https"), createHttpTransport("bar", "http"));

        Assert.assertEquals(createHttpTransport("foo", "https"), createHttpTransport("foo", "http"));
    }

    private static HttpTransport createHttpTransport(String id, String scheme) {
        return new HttpTransport(id, scheme, "localhost", 9292);
    }
}
