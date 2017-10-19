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
 * @since 0.10.5
 */
public class ThemeTest {

    @DataProvider
    public Object[][] mergeableThemes() {
        return new Object[][]{
                {new Theme("t1", "p1"), new Theme("t1", "p2")},
                {new Theme("t1", emptyList()), new Theme("t1", emptyList())},
                {new Theme("t1", singletonList("p1")), new Theme("t1", singletonList("p2"))},
                {new Theme("t1", "p1"), new Theme("t1", emptyList())},
                {new Theme("t1", singletonList("p1")), new Theme("t1", "p2")}
        };
    }

    @Test(dataProvider = "mergeableThemes")
    public void testMergeability(Theme theme1, Theme theme2) {
        Assert.assertTrue(theme1.isMergeable(theme2), theme1 + " should be able to merge with " + theme2 + ".");
        Assert.assertTrue(theme2.isMergeable(theme1), theme2 + " should be able to merge with " + theme1 + ".");
    }

    @DataProvider
    public Object[][] unmergeableThemes() {
        return new Object[][]{
                {new Theme("t1", "p1"), new Theme("t2", "p1")},
                {new Theme("t1", emptyList()), new Theme("t2", emptyList())},
                {new Theme("t1", singletonList("p1")), new Theme("t2", singletonList("p1"))},
                {new Theme("t1", "p1"), new Theme("t2", "p2")}
        };
    }

    @Test(dataProvider = "unmergeableThemes")
    public void testUnmergeability(Theme theme1, Theme theme2) {
        Assert.assertFalse(theme1.isMergeable(theme2), theme1 + " shouldn't be able to merge with " + theme2 + ".");
        Assert.assertFalse(theme2.isMergeable(theme1), theme2 + " shouldn't be able to merge with " + theme1 + ".");

        Assert.assertThrows(IllegalArgumentException.class, () -> theme1.merge(theme2));
        Assert.assertThrows(IllegalArgumentException.class, () -> theme2.merge(theme1));
    }

    @DataProvider
    public Object[][] mergingThemes() {
        return new Object[][]{
                {new Theme("t1", "p1"), new Theme("t1", "p2"), new Theme("t1", asList("p2", "p1"))},
                {new Theme("t1", emptyList()), new Theme("t1", emptyList()), new Theme("t1", emptyList())},
                {new Theme("t1", singletonList("p1")), new Theme("t1", singletonList("p2")),
                        new Theme("t1", asList("p2", "p1"))},
                {new Theme("t1", asList("p1", "p11")), new Theme("t1", asList("p2", "p22")),
                        new Theme("t1", asList("p2", "p22", "p1", "p11"))}
        };
    }

    @Test(dataProvider = "mergingThemes")
    public void testMerge(Theme theme, Theme otherTheme, Theme mergedTheme) {
        Assert.assertEquals(theme.merge(otherTheme), mergedTheme);
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
