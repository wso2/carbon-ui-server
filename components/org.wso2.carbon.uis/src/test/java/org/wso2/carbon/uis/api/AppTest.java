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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uis.api.http.HttpRequest;
import org.wso2.carbon.uis.internal.exception.PageNotFoundException;
import org.wso2.carbon.uis.internal.exception.PageRedirectException;
import org.wso2.carbon.uis.internal.impl.HtmlPage;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link App} class.
 *
 * @since 0.10.0
 */
public class AppTest {

    @Test
    public void testGetExtension() {
        App app = createApp();
        Assert.assertTrue(app.getExtension("type1", "ext1").isPresent());
        Assert.assertFalse(app.getExtension("type1", "ext2").isPresent());
    }

    @Test
    public void testGetTheme() {
        App app = createApp();
        Assert.assertTrue(app.getTheme("theme1").isPresent());
        Assert.assertFalse(app.getTheme("theme3").isPresent());
    }

    @Test
    public void testRenderPageExisting() {
        App app = createApp();
        Assert.assertEquals(app.renderPage(createRequest("/")), "index page");
        Assert.assertEquals(app.renderPage(createRequest("/index")), "index page");
        Assert.assertEquals(app.renderPage(createRequest("/a")), "page A");
        Assert.assertEquals(app.renderPage(createRequest("/b/")), "page B");
        Assert.assertEquals(app.renderPage(createRequest("/b/index")), "page B");
    }

    @Test
    public void testRenderPageRedirection() {
        final App app = createApp();
        HttpRequest request = mock(HttpRequest.class);
        when(request.getContextPath()).thenReturn("/app");
        when(request.getQueryString()).thenReturn("param1=value1&param2=value2");

        // remove extra slash
        when(request.getUriWithoutContextPath()).thenReturn("/a/");
        PageRedirectException pageRedirectException = Assert.expectThrows(PageRedirectException.class,
                                                                          () -> app.renderPage(request));
        Assert.assertEquals(pageRedirectException.getRedirectUrl(),
                            request.getContextPath() + "/a?" + request.getQueryString(),
                            "URL correction is incorrect.");

        // add missing slash
        when(request.getUriWithoutContextPath()).thenReturn("/b");
        pageRedirectException = Assert.expectThrows(PageRedirectException.class, () -> app.renderPage(request));
        Assert.assertEquals(pageRedirectException.getRedirectUrl(),
                            request.getContextPath() + "/b/?" + request.getQueryString(),
                            "URL correction is incorrect.");
    }

    @Test
    public void testRenderPageNotFound() {
        App app = createApp();
        Assert.assertThrows(PageNotFoundException.class, () -> app.renderPage(createRequest("/foo")));
        Assert.assertThrows(PageNotFoundException.class, () -> app.renderPage(createRequest("/bar")));
    }

    private static App createApp() {
        SortedSet<Page> pages = ImmutableSortedSet.of(createPage("/", "index page"),
                                                      createPage("/a", "page A"),
                                                      createPage("/b/", "page B"));
        Set<Extension> extensions = ImmutableSet.of(new Extension("ext1", "type1", null),
                                                    new Extension("ext2", "type2", null));
        Set<Theme> themes = ImmutableSet.of(new Theme("theme1", "p1"), new Theme("theme2", "p1"));
        return new App(null, null, pages, extensions, themes, Collections.emptySet(), null, null);
    }

    private static Page createPage(String uriPattern, String content) {
        return new HtmlPage(new UriPatten(uriPattern), content);
    }

    private static HttpRequest createRequest(String uriWithoutContextPath) {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getUriWithoutContextPath()).thenReturn(uriWithoutContextPath);
        return request;
    }
}
