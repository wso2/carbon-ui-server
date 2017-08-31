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

import com.google.common.collect.ImmutableMap;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

import static java.lang.Integer.signum;

/**
 * Test cases for URI pattern.
 *
 * @since 1.0.0
 */
public class UriPattenTest {

    @DataProvider
    public Object[][] invalidUriPatterns() {
        return new Object[][]{
                {"", "URI pattern cannot be empty."},
                {"a", "URI patten must start with a '/'."},
                {"/{", "at index 1"},
                {"/{a", "at index 1"},
                {"/{{abc", "at index 2"},
                {"/}", "at index 1"},
                {"/a}", "at index 2"},
                {"/a}}", "at index 2"},
                {"/{a}{", "at index 4"},
                {"/{a}}", "at index 4"},
                {"/{ab{c}}", "at index 4"},
                {"/{+a}}", "at index 5"},
                {"/{+a}{", "at index 5"},
                {"/{+ab{c}}", "at index 5"},
                {"/{+a}/", "at index 5"}
        };
    }

    @DataProvider
    public Object[][] matchingUriPatterns() {
        return new Object[][]{
                {"/", "/"},
                {"/a", "/a"},
                {"/-._~?#[]@!$&'()+,;=", "/-._~?#[]@!$&'()+,;="},
                {"/{x}", "/a"},
                {"/{x}", "/-._~?#[]@!$&'()+,;="},
                {"/a{x}", "/ab"},
                {"/{x}b", "/ab"},
                {"/a/{x}", "/a/b"},
                {"/a{x}c/d{y}f", "/abc/def"},
                {"/{+x}", "/a"},
                {"/{+x}", "/-._~?#[]@!$&'()+,;="},
                {"/{+x}", "/a/"},
                {"/{+x}", "/a/b/c"},
                {"/a/{x}/{+y}", "/a/b/c/d"},
                {"/a/{x}/c/de{+y}", "/a/b/c/def/g/"},
                {"/index", "/"},
                {"/index", "/index"},
                {"/a/index", "/a/"},
                {"/a/index", "/a/index"},
                {"/a{x}/index", "/ab/"},
                {"/a{x}/index", "/ab/index"},
                {"/a/{x}/{y}/index", "/a/b/c/"},
                {"/a/{x}/{y}/index", "/a/b/c/index"},
                {"/a/{x}/index", "/a/b/"},
                {"/a/{x}/index", "/a/b/index"}
        };
    }

    @DataProvider
    public Object[][] unmatchingUriPatterns() {
        return new Object[][]{
                {"/", "/a"},
                {"/a", "/abc"},
                {"/a/", "/a"},
                {"/{x}", "/a/b"},
                {"/{x}/", "/a/b"},
                {"/a/b/{x}", "/a/b/c/d"},
                {"/a{+x}", "/A/b"},
                {"/a/{+x}", "/A/b"},
                {"/a/b{+x}", "/a/Bc/d"},
                {"/index", "/index/"},
                {"/a/index", "/a/index/"},
                {"/a/{x}/index", "/a/b/index/"}
        };
    }

    @DataProvider
    public Object[][] orderedUriPatterns() {
        return new Object[][]{
                {"/a", "/{a}"},
                {"/a/b", "/{a}/b"},
                {"/a/b", "/a/{b}"},
                {"/a/b/", "/a/{b}/"},
                {"/a/b", "/{a}/{b}"},
                {"/a/b/", "/{a}/{b}/"},
                {"/{a}/b", "/{a}/{b}"},
                {"/a/{b}", "/{a}/{b}"},
                {"/ab", "/a{b}"},
                {"/ab", "/{a}b"}
        };
    }

    @DataProvider
    public Object[][] uriPatternsWithSingleParams() {
        return new Object[][]{
                {"/{x}", "/a", "x", "a"},
                {"/{x}", "/-._~?#[]@!$&'()+,;=", "x", "-._~?#[]@!$&'()+,;="},
                {"/a{x}", "/ab", "x", "b"},
                {"/{x}b", "/ab", "x", "a"},
                {"/a/{x}", "/a/b", "x", "b"},
                {"/a{x}/index", "/ab/", "x", "b"},
                {"/a{x}/index", "/ab/index", "x", "b"},
                {"/a/{x}/index", "/a/b/", "x", "b"},
                {"/a/{x}/index", "/a/b/index", "x", "b"}
        };
    }

    @DataProvider
    public Object[][] uriPatternsWithMultipleParams() {
        return new Object[][]{
                {"/a{x}c/d{y}f", "/abc/def", ImmutableMap.of("x", "b", "y", "e")},
                {"/{+x}", "/a", ImmutableMap.of("x", "a")},
                {"/{+x}", "/-._~?#[]@!$&'()+,;=", ImmutableMap.of("x", "-._~?#[]@!$&'()+,;=")},
                {"/{+x}", "/a/", ImmutableMap.of("x", "a/")},
                {"/{+x}", "/a/b/c", ImmutableMap.of("x", "a/b/c")},
                {"/a/{x}/{+y}", "/a/b/c/d", ImmutableMap.of("x", "b", "y", "c/d")},
                {"/a/{x}/c/de{+y}", "/a/b/c/def/g/", ImmutableMap.of("x", "b", "y", "f/g/")},
                {"/a/{x}/{y}/index", "/a/b/c/", ImmutableMap.of("x", "b", "y", "c")},
                {"/a/{x}/{y}/index", "/a/b/c/index", ImmutableMap.of("x", "b", "y", "c")}
        };
    }

    @Test(dataProvider = "invalidUriPatterns")
    public void testInvalidUriPatterns(String uriPattern, String message) {
        try {
            new UriPatten(uriPattern);
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains(message));
        }
    }

    @Test(dataProvider = "matchingUriPatterns")
    public void testMatching(String uriPattern, String uri) {
        Assert.assertTrue(new UriPatten(uriPattern).matches(uri));
    }

    @Test(dataProvider = "unmatchingUriPatterns")
    public void testUnmatching(String uriPattern, String uri) {
        Assert.assertFalse(new UriPatten(uriPattern).matches(uri));
    }

    @Test(dataProvider = "orderedUriPatterns")
    public void testOrdering(String a, String b) {
        UriPatten aPatten = new UriPatten(a);
        UriPatten bPatten = new UriPatten(b);
        int i = aPatten.compareTo(bPatten);
        int j = bPatten.compareTo(aPatten);
        Assert.assertTrue(i < 0, a + " should be more specific than " + b);
        Assert.assertTrue(j > 0, a + " should be more specific than " + b);
    }

    @Test(dataProvider = "matchingUriPatterns")
    public void testPatternsWithParameters(String uriPattern, String uri) {
        Assert.assertTrue(new UriPatten(uriPattern).match(uri).isPresent());
    }

    @Test(dataProvider = "uriPatternsWithSingleParams")
    public void testPatternsWithSingleParameters(String uriPattern, String uri, String paramKey, String paramVal) {
        Assert.assertTrue(new UriPatten(uriPattern).match(uri).get().get(paramKey).equals(paramVal));
    }

    @Test(dataProvider = "uriPatternsWithMultipleParams")
    public void testPatternsWithMultipleParameters(String uriPattern, String uri, Map<String, String> data) {
        data.forEach((paramKey, paramVal) ->
                             Assert.assertTrue(new UriPatten(uriPattern).match(uri).get().get(paramKey)
                                                       .equals(paramVal)));
    }

    @Test(dataProvider = "unmatchingUriPatterns")
    public void testUnMatchingPatternsWithParameters(String uriPattern, String uri) {
        Assert.assertFalse(new UriPatten(uriPattern).match(uri).isPresent());
    }

    @Test
    public void testInvariants() {
        UriPatten[] pattens = new UriPatten[]{
                new UriPatten("/"),
                new UriPatten("/a"),
                new UriPatten("/ab"),
                new UriPatten("/a{b}"),
                new UriPatten("/{a}/{b}"),
                new UriPatten("/{a}"),
                new UriPatten("/{a}"),
                new UriPatten("/a/{b}"),
                new UriPatten("/ab/{b}"),
                new UriPatten("/{a}/b"),
        };
        // following invariants are specified in java.lang.Comparable
        for (int x = 0; x < pattens.length; x++) {
            for (int y = x + 1; y < pattens.length; y++) {
                int xy = pattens[x].compareTo(pattens[y]);
                int yx = pattens[y].compareTo(pattens[x]);
                Assert.assertTrue(signum(xy) == -signum(yx));
                for (int z = y + 1; z < pattens.length; z++) {
                    int xz = pattens[x].compareTo(pattens[z]);
                    int yz = pattens[y].compareTo(pattens[z]);
                    if (xy > 0 && yz > 0) {
                        Assert.assertTrue(xz > 0);
                    } else if (yx > 0 && yz > 0) {
                        Assert.assertTrue(yz > 0);
                    } else if (xy == 0) {
                        Assert.assertTrue(signum(xz) == signum(yz));
                    }
                }
            }
        }
    }
}
