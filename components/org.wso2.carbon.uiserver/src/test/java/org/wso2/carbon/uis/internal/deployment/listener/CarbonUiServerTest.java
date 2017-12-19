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

package org.wso2.carbon.uis.internal.deployment.listener;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uis.api.App;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link CarbonUiServer} class.
 *
 * @since 0.15.0
 */
public class CarbonUiServerTest {

    @Test
    public void testGetApp() {
        CarbonUiServer carbonUiServer = new CarbonUiServer();

        carbonUiServer.appDeploymentEvent(createApp("foo"));
        Assert.assertTrue(carbonUiServer.getApp("foo").isPresent());

        Assert.assertFalse(carbonUiServer.getApp("bar").isPresent());
        carbonUiServer.appDeploymentEvent(createApp("bar"));
        Assert.assertTrue(carbonUiServer.getApp("bar").isPresent());

        carbonUiServer.appUndeploymentEvent("foo");
        Assert.assertFalse(carbonUiServer.getApp("foo").isPresent());
        Assert.assertTrue(carbonUiServer.getApp("bar").isPresent());

        carbonUiServer.close();
        Assert.assertFalse(carbonUiServer.getApp("foo").isPresent());
        Assert.assertFalse(carbonUiServer.getApp("bar").isPresent());
    }

    private static App createApp(String appName) {
        App app = mock(App.class);
        when(app.getName()).thenReturn(appName);
        return app;
    }
}
