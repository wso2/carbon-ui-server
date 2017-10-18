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

/**
 * Test cases for {@link Extension} class.
 *
 * @since 0.10.5
 */
public class ExtensionTest {

    @DataProvider
    public Object[][] mergeableExtensions() {
        return new Object[][]{
                {new Extension("e1", "t1", null), new Extension("e1", "t1", null)},
                {new Extension("e1", "t1", "p1"), new Extension("e1", "t1", null)},
                {new Extension("e1", "t1", null), new Extension("e1", "t1", "p2")},
                {new Extension("e1", "t1", "p1"), new Extension("e1", "t1", "p2")}
        };
    }

    @Test(dataProvider = "mergeableExtensions")
    public void testMergeability(Extension extension1, Extension extension2) {
        Assert.assertTrue(extension1.isMergeable(extension2),
                          extension1 + " should be able to merge with " + extension2 + ".");
        Assert.assertTrue(extension2.isMergeable(extension1),
                          extension2 + " should be able to merge with " + extension1 + ".");
    }

    @DataProvider
    public Object[][] unmergeableExtension() {
        return new Object[][]{
                {new Extension("e1", "t1", null), new Extension("e1", "t2", null)},
                {new Extension("e1", "t1", null), new Extension("e2", "t2", null)},
                {new Extension("e1", "t1", "p1"), new Extension("e1", "t2", "p1")},
                {new Extension("e1", "t1", "p1"), new Extension("e2", "t2", "p2")}
        };
    }

    @Test(dataProvider = "unmergeableExtension")
    public void testUnmergeability(Extension extension1, Extension extension2) {
        Assert.assertFalse(extension1.isMergeable(extension2),
                           extension1 + " shouldn't be able to merge with " + extension2 + ".");
        Assert.assertFalse(extension2.isMergeable(extension1),
                           extension2 + " shouldn't be able to merge with " + extension1 + ".");

        Assert.assertThrows(IllegalArgumentException.class, () -> extension1.merge(extension2));
        Assert.assertThrows(IllegalArgumentException.class, () -> extension2.merge(extension1));
    }

    @DataProvider
    public Object[][] mergingExtensions() {
        return new Object[][]{
                {new Extension("e1", "t1", null), new Extension("e1", "t1", null), new Extension("e1", "t1", null)},
                {new Extension("e1", "t1", "p1"), new Extension("e1", "t1", null), new Extension("e1", "t1", null)},
                {new Extension("e1", "t1", null), new Extension("e1", "t1", "p2"), new Extension("e1", "t1", "p2")},
                {new Extension("e1", "t1", "p1"), new Extension("e1", "t1", "p2"), new Extension("e1", "t1", "p2")}
        };
    }

    @Test(dataProvider = "mergingExtensions")
    public void testMerge(Extension extension, Extension otherExtension, Extension mergedExtension) {
        Assert.assertEquals(extension.merge(otherExtension), mergedExtension);
    }

    @DataProvider
    public Object[][] equalExtensions() {
        Extension theme = new Extension("e1", "t1", null);
        return new Object[][]{
                {theme, theme},
                {new Extension("e1", "t1", null), new Extension("e1", "t1", null)},
                {new Extension("e1", "t1", "p1"), new Extension("e1", "t1", "p1")},
        };
    }

    @Test(dataProvider = "equalExtensions")
    public void testEquals(Extension extension1, Extension extension2) {
        Assert.assertEquals(extension1, extension2);
    }
}
