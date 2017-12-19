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

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.uiserver.internal.http.util.IpAddressUtils;

/**
 * Test cases for {@link HttpTransport} class.
 *
 * @since 0.15.0
 */
public class HttpTransportTest {

    @Test
    public void test() {
        final String listenerInterfaceId = "foo";
        final String listenerConfigurationId = "bar";
        final String scheme = "http";
        final String host = "localhost";
        final int port = 9292;

        HttpTransport httpTransport = new HttpTransport(listenerInterfaceId, listenerConfigurationId, scheme, host,
                                                        port);
        Assert.assertEquals(httpTransport.getListenerInterfaceId(), listenerInterfaceId);
        Assert.assertEquals(httpTransport.getListenerConfigurationId(), listenerConfigurationId);
        Assert.assertEquals(httpTransport.getScheme(), scheme);
        Assert.assertEquals(httpTransport.getHost(), host);
        Assert.assertEquals(httpTransport.getPort(), port);
    }

    @Test
    public void testIsSecured() {
        Assert.assertFalse(createHttpTransport("foo", "bar", "http").isSecured());
        Assert.assertTrue(createHttpTransport("bar", "foo", "HTTPS").isSecured());
    }

    @DataProvider
    public Object[][] httpTransports() {
        return new Object[][]{
                {createHttpTransport("localhost"), IpAddressUtils.getLocalIpAddress().orElse("localhost")},
                {createHttpTransport("127.0.0.1"), IpAddressUtils.getLocalIpAddress().orElse("127.0.01")},
                {createHttpTransport("0.0.0.0"), IpAddressUtils.getLocalIpAddress().orElse("0.0.0.0")},
                {createHttpTransport("::1"), IpAddressUtils.getLocalIpAddress().orElse("::1")},
                {createHttpTransport("192.168.1.1"), "192.168.1.1"}
        };
    }

    @Test(dataProvider = "httpTransports")
    public void testGetUrlFor(HttpTransport httpTransport, String hostname) {
        Assert.assertEquals(httpTransport.getUrlFor("/test"), "http://" + hostname + ":9090/test");
    }

    @Test
    public void testEquals() {
        Assert.assertNotEquals(null, createHttpTransport("foo", "bar", "http"));
        Assert.assertNotEquals(new Object(), createHttpTransport("foo", "bar", "http"));
        Assert.assertNotEquals(createHttpTransport("foo", "bar", "https"), createHttpTransport("foo2", "bar", "http"));
        Assert.assertNotEquals(createHttpTransport("foo", "bar", "https"), createHttpTransport("foo2", "bar2", "http"));

        Assert.assertEquals(createHttpTransport("foo", "bar", "https"), createHttpTransport("foo", "bar", "http"));
    }

    private static HttpTransport createHttpTransport(String listenerInterfaceId, String listenerConfigurationId,
                                                     String scheme) {
        return new HttpTransport(listenerInterfaceId, listenerConfigurationId, scheme, "localhost", 9292);
    }

    private static HttpTransport createHttpTransport(String host) {
        return new HttpTransport("foo", "bar", "http", host, 9090);
    }
}
