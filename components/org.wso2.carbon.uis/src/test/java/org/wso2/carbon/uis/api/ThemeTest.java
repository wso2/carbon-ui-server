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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * Test cases for {@link Theme} class.
 *
 * @since 0.12.0
 */
public class ThemeTest {

    @DataProvider
    public Object[][] overridableThemes() {
        return new Object[][]{
                {new Theme("t1", "p1"), new Theme("t1", "p2")},
                {new Theme("t1", emptyList()), new Theme("t1", singletonList("p2"))},
                {new Theme("t1", asList("p1", "p11")), new Theme("t1", asList("p2", "p22"))}
        };
    }

    @Test(dataProvider = "overridableThemes")
    public void testCanOverrideBy(Theme theme1, Theme theme2) {
        Assert.assertTrue(theme1.canOverrideBy(theme2));
    }

    @DataProvider
    public Object[][] equalThemes() {
        Theme theme = new Theme("t0", "p0");
        return new Object[][]{
                {theme, theme},
                {new Theme("t1", "p1"), new Theme("t1", "p1")},
                {new Theme("t1", emptyList()), new Theme("t1", emptyList())},
                {new Theme("t1", singletonList("p1")), new Theme("t1", singletonList("p1"))},
                {new Theme("t1", asList("p1", "p11")), new Theme("t1", asList("p1", "p11"))}
        };
    }

    @Test(dataProvider = "equalThemes")
    public void testEquals(Theme theme1, Theme theme2) {
        Assert.assertEquals(theme1, theme2);
    }
}
