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

package org.wso2.carbon.uis.api;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

import static org.wso2.carbon.uis.api.http.HttpResponse.HEADER_CACHE_CONTROL;
import static org.wso2.carbon.uis.api.http.HttpResponse.HEADER_EXPIRES;
import static org.wso2.carbon.uis.api.http.HttpResponse.HEADER_PRAGMA;

import static java.util.Collections.singletonMap;

/**
 * Test cases for {@link Configuration} class.
 *
 * @since 0.12.5
 */
public class ConfigurationTest {

    @Test
    public void testGetResponseHeaders() {
        Map<String, String> pages = singletonMap(HEADER_EXPIRES, "100");
        Map<String, String> staticResources = singletonMap(HEADER_CACHE_CONTROL, "public,max-age=100");
        Configuration configuration = new Configuration(new Configuration.HttpResponseHeaders(pages, staticResources));

        Assert.assertEquals(configuration.getResponseHeaders().forPages().get(HEADER_PRAGMA),
                            Configuration.DEFAULT_CONFIGURATION.getResponseHeaders().forPages().get(HEADER_PRAGMA));
        Assert.assertEquals(configuration.getResponseHeaders().forPages().get(HEADER_EXPIRES),
                            pages.get(HEADER_EXPIRES));
        Assert.assertEquals(configuration.getResponseHeaders().forStaticResources().get(HEADER_CACHE_CONTROL),
                            staticResources.get(HEADER_CACHE_CONTROL));
    }
}
