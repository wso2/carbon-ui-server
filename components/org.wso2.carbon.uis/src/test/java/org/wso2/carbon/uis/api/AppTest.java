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
import org.wso2.carbon.uis.api.exception.PageNotFoundException;
import org.wso2.carbon.uis.api.exception.PageRedirectException;
import org.wso2.carbon.uis.api.http.HttpRequest;
import org.wso2.carbon.uis.internal.impl.HtmlPage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
        App app = appBuilder()
                .addExtension(new Extension("ext1", "type1", "path1"))
                .addExtension(new Extension("ext2", "type2", "path2"))
                .build();
        Assert.assertTrue(app.getExtension("type1", "ext1").isPresent());
        Assert.assertFalse(app.getExtension("type1", "ext2").isPresent());
    }

    @Test
    public void testGetTheme() {
        App app = appBuilder()
                .addTheme(new Theme("theme1", "path1"))
                .addTheme(new Theme("theme2", "path2"))
                .build();
        Assert.assertTrue(app.getTheme("theme1").isPresent());
        Assert.assertFalse(app.getTheme("theme3").isPresent());
    }

    @Test
    public void testRenderPageExisting() {
        App app = appBuilder()
                .addPage(createPage("/", "index page"))
                .addPage(createPage("/a", "page A"))
                .addPage(createPage("/b/", "page B"))
                .build();
        Assert.assertEquals(app.renderPage(createRequest("/")), "index page");
        Assert.assertEquals(app.renderPage(createRequest("/index")), "index page");
        Assert.assertEquals(app.renderPage(createRequest("/a")), "page A");
        Assert.assertEquals(app.renderPage(createRequest("/b/")), "page B");
        Assert.assertEquals(app.renderPage(createRequest("/b/index")), "page B");
    }

    @Test
    public void testRenderPageRedirection() {
        App app = appBuilder()
                .addPage(createPage("/a", "page A"))
                .addPage(createPage("/b/", "page B"))
                .build();
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
        App app = appBuilder().build();
        Assert.assertThrows(PageNotFoundException.class, () -> app.renderPage(createRequest("/foo")));
        Assert.assertThrows(PageNotFoundException.class, () -> app.renderPage(createRequest("/bar")));
    }

    @Test
    public void testCanOverrideBy() {
        App app1 = appBuilder()
                .setName("foo")
                .setContextPath("/foo")
                .build();
        App app2 = appBuilder()
                .setName("foo")
                .setContextPath("/foo")
                .build();
        App app3 = appBuilder()
                .setName("bar")
                .setContextPath("/bar")
                .build();

        Assert.assertTrue(app1.canOverrideBy(app2));
        Assert.assertFalse(app1.canOverrideBy(app3));
        Assert.assertFalse(app2.canOverrideBy(app3));
    }

    @Test
    public void testOverride() {
        App app1 = appBuilder()
                .setName("foo")
                .setContextPath("/foo")
                .build();
        App app2 = appBuilder()
                .setName("foo")
                .setContextPath("/foo")
                .build();
        App overriddenApp = app1.override(app2);
        Assert.assertEquals(overriddenApp.getName(), app1.getName());
        Assert.assertEquals(overriddenApp.getContextPath(), app1.getContextPath());

        App app3 = appBuilder()
                .setName("bar")
                .setContextPath("/bar")
                .build();
        Assert.assertThrows(IllegalArgumentException.class, () -> app1.override(app3));
    }

    private static Page createPage(String uriPattern, String content) {
        return new HtmlPage(new UriPatten(uriPattern), content);
    }

    private static HttpRequest createRequest(String uriWithoutContextPath) {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getUriWithoutContextPath()).thenReturn(uriWithoutContextPath);
        return request;
    }

    private static AppBuilder appBuilder() {
        return new AppBuilder();
    }

    /**
     * Builder for {@link App} class.
     *
     * @since 0.12.5
     */
    private static class AppBuilder {

        private String name;
        private String contextPath;
        private SortedSet<Page> pages = new TreeSet<>();
        private Set<Extension> extensions = new HashSet<>();
        private Set<Theme> themes = new HashSet<>();
        private Set<I18nResource> i18nResources = new HashSet<>();
        private Configuration configuration = Configuration.DEFAULT_CONFIGURATION;
        private List<String> paths = new ArrayList<>();

        public AppBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public AppBuilder setContextPath(String contextPath) {
            this.contextPath = contextPath;
            return this;
        }

        public AppBuilder addPage(Page page) {
            this.pages.add(page);
            return this;
        }

        public AppBuilder addExtension(Extension extension) {
            this.extensions.add(extension);
            return this;
        }

        public AppBuilder addTheme(Theme theme) {
            this.themes.add(theme);
            return this;
        }

        public AppBuilder addI18nResource(I18nResource i18nResource) {
            this.i18nResources.add(i18nResource);
            return this;
        }

        public AppBuilder setConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        public AppBuilder setPath(String path) {
            this.paths = Collections.singletonList(path);
            return this;
        }

        public AppBuilder addPath(String path) {
            this.paths.add(path);
            return this;
        }

        public App build() {
            return new App(name, contextPath, pages, extensions, themes, i18nResources, configuration, paths);
        }
    }
}
