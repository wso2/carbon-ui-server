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

import static java.lang.Integer.signum;

/**
 * Test cases for {@link Page} class.
 *
 * @since 0.10.0
 */
public class PageTest {

    @DataProvider
    public Object[][] matchingPagesAndUris() {
        return new Object[][]{
                {createPage("/"), "/"},
                {createPage("/"), "/index"},
                {createPage("/a"), "/a"},
                {createPage("/a/"), "/a/"},
                {createPage("/a/"), "/a/index"},
                {createPage("/a{x}"), "/abc"},
                {createPage("/a/{x}"), "/a/b"},
        };
    }

    @DataProvider
    public Object[][] orderedPages() {
        return new Object[][]{
                {createPage("/a"), createPage("/{x}")},
                {createPage("/ab"), createPage("/a{x}")},
                {createPage("/ab"), createPage("/a{x}")}
        };
    }

    @DataProvider
    public Object[][] triplePages() {
        return new Object[][]{
                {createPage("/"), createPage("/"), createPage("/")},
                {createPage("/a"), createPage("/a"), createPage("/a")},
                {createPage("/a/"), createPage("/a/"), createPage("/a/")},
                {createPage("/a/index"), createPage("/a/index"), createPage("/a/index")},
                {createPage("/a"), createPage("/b"), createPage("/c")},
                {createPage("/a"), createPage("/a{x}"), createPage("/ab{x}")}
        };
    }

    @Test(dataProvider = "matchingPagesAndUris")
    public void testMatches(Page page, String uri) {
        Assert.assertTrue(page.matches(uri), "Page " + page + " should match with '" + uri + "' URI.");
    }

    @Test(dataProvider = "orderedPages")
    public void testOrdering(Page pageA, Page pageB) {
        Assert.assertTrue(pageA.compareTo(pageB) < 0, "As page " + pageA + " is more specific than page " + pageB +
                                                      ", it should come first in the order.");
        Assert.assertTrue(pageB.compareTo(pageA) > 0, "As page " + pageB + " is less specific than page " + pageA +
                                                      ", it should come last in the order.");
    }

    @Test(dataProvider = "triplePages",
          description = "Tests invariants specified in java.lang.Comparable interface")
    public void testInvariants(Page pageA, Page pageB, Page pageC) {
        // sgn(A.compareTo(B)) == -sgn(B.compareTo(A))
        Assert.assertEquals(signum(pageA.compareTo(pageB)), -1 * signum(pageB.compareTo(pageA)),
                            "sgn(A.compareTo(B)) == -sgn(B.compareTo(A)) should be true.");

        // (A.compareTo(B)>0 && B.compareTo(C)>0) implies A.compareTo(C)>0
        if ((pageA.compareTo(pageB) > 0) && (pageB.compareTo(pageC) > 0)) {
            Assert.assertTrue(pageA.compareTo(pageC) > 0,
                              "If (A.compareTo(B)>0 && B.compareTo(C)>0) then A.compareTo(C)>0 should be true.");
        }
        // (A.compareTo(B)<0 && B.compareTo(C)<0) implies A.compareTo(C)<0
        if ((pageA.compareTo(pageB) < 0) && (pageB.compareTo(pageC) < 0)) {
            Assert.assertTrue(pageA.compareTo(pageC) < 0,
                              "If (A.compareTo(B)<0 && B.compareTo(C)<0) then A.compareTo(C)<0 should be true.");
        }

        // A.compareTo(B)==0 implies that sgn(A.compareTo(C)) == sgn(B.compareTo(C)), for all C
        if (pageA.compareTo(pageB) == 0) {
            Assert.assertEquals(signum(pageA.compareTo(pageC)), signum(pageB.compareTo(pageC)),
                                "If A.compareTo(B)==0 then sgn(A.compareTo(C)) == sgn(B.compareTo(C)) should be true.");
        }

        // (A.compareTo(B)==0) == (A.equals(B))
        if (pageA.compareTo(pageB) == 0) {
            Assert.assertTrue(pageA.equals(pageB), "If A.compareTo(B)==0 then A.equals(B) should be true.");
        }
        if (pageB.compareTo(pageC) == 0) {
            Assert.assertTrue(pageB.equals(pageC), "If B.compareTo(C)==0 then B.equals(C) should be true.");
        }
    }

    private static Page createPage(String uriPattern) {
        return new Page(new UriPatten(uriPattern), null);
    }
}
