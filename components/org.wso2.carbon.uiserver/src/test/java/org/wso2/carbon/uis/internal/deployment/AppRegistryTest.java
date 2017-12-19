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

package org.wso2.carbon.uis.internal.deployment;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uis.api.App;

import java.util.Collections;

/**
 * Test cases for {@link AppRegistry} class.
 *
 * @since 0.12.0
 */
public class AppRegistryTest {

    @Test
    public void testAddGet() {
        AppRegistry appRegistry = new AppRegistry();
        App a1 = createApp("a1", "p1");
        String k1 = appRegistry.add(a1);
        App a2 = createApp("a2", "p2");
        appRegistry.add(a2);

        Assert.assertTrue(appRegistry.get(k1).isPresent(), "App " + a1 + " should be in the app registry.");
        Assert.assertEquals(appRegistry.get(k1).orElse(null), a1);
        Assert.assertFalse(appRegistry.get("some-key").isPresent(),
                           "There shouldn't be an app for key 'some-key' in the app registry.");
    }

    @Test
    public void testGetAll() {
        AppRegistry appRegistry = new AppRegistry();
        App a1 = createApp("a1", "p1");
        appRegistry.add(a1);
        App a2 = createApp("a2", "p2");
        appRegistry.add(a2);

        Assert.assertEquals(appRegistry.getAll().size(), 2);
        Assert.assertTrue(appRegistry.getAll().contains(a1), "App " + a1 + " should be in the app registry.");
        Assert.assertTrue(appRegistry.getAll().contains(a2), "App " + a2 + " should be in the app registry.");
    }

    @Test
    public void testRemove() {
        AppRegistry appRegistry = new AppRegistry();
        App a1 = createApp("a1", "p1");
        String k1 = appRegistry.add(a1);
        App a2 = createApp("a2", "p2");
        appRegistry.add(a2);
        App a3 = createApp("a3", "p3");

        Assert.assertTrue(appRegistry.remove(k1).isPresent(), "App " + a1 + " should be in the app registry.");
        Assert.assertFalse(appRegistry.remove("some-key").isPresent(),
                           "There shouldn't be an app for key 'some-key' in the app registry.");
        Assert.assertTrue(appRegistry.remove(a2), "App " + a2 + " should be in the app registry.");
        Assert.assertFalse(appRegistry.remove(a3), "App " + a3 + " shouldn't be in the app registry.");
    }

    @Test
    public void testFind() {
        AppRegistry appRegistry = new AppRegistry();
        App a1 = createApp("a1", "p1");
        appRegistry.add(a1);
        App a2 = createApp("a2", "p2");
        appRegistry.add(a2);

        Assert.assertTrue(appRegistry.find(app -> a1 == app).isPresent(),
                          "App " + a1 + " should be in the app registry.");
        Assert.assertFalse(appRegistry.find(app -> app.getName().equals("foo")).isPresent());
    }

    @Test
    public void testClear() {
        AppRegistry appRegistry = new AppRegistry();
        App a1 = createApp("a1", "p1");
        String k1 = appRegistry.add(a1);
        App a2 = createApp("a2", "p2");
        appRegistry.add(a2);

        appRegistry.clear();
        Assert.assertFalse(appRegistry.get(k1).isPresent(),
                           "App " + a1 + " shouldn't be in the app registry after clearing.");
        Assert.assertEquals(appRegistry.getAll().size(), 0);
        Assert.assertFalse(appRegistry.remove(k1).isPresent(),
                          "App " + a1 + " shouldn't be in the app registry after clearing.");
        Assert.assertFalse(appRegistry.remove(a2), "App " + a2 + " shouldn't be in the app registry after clearing.");
    }

    private static App createApp(String name, String path) {
        return new App(name, "/" + name, Collections.emptySortedSet(), Collections.emptySet(), Collections.emptySet(),
                       Collections.emptySet(), null, path);
    }
}
