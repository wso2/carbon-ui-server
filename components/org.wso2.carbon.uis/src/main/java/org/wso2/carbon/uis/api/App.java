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

import org.wso2.carbon.uis.api.exception.PageNotFoundException;
import org.wso2.carbon.uis.api.exception.PageRedirectException;
import org.wso2.carbon.uis.api.http.HttpRequest;
import org.wso2.carbon.uis.api.util.Multilocational;
import org.wso2.carbon.uis.api.util.Overridable;
import org.wso2.carbon.uis.internal.impl.OverriddenApp;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

/**
 * Represents a web app.
 *
 * @since 0.8.0
 */
public class App implements Multilocational, Overridable<App> {

    private final String name;
    private final String contextPath;
    private final SortedSet<Page> pages;
    private final Map<String, Extension> extensions;
    private final Map<String, Theme> themes;
    private final Map<Locale, I18nResource> i18nResources;
    private final Configuration configuration;
    private final List<String> paths;

    /**
     * Creates a new app which can be located in the specified path.
     *
     * @param name          name of the app
     * @param contextPath   context path of the app
     * @param pages         pages of the app
     * @param extensions    extensions of the app
     * @param themes        themes of the app
     * @param i18nResources i18n resources of the app
     * @param configuration configurations of the app
     * @param path          path to the app
     */
    public App(String name, String contextPath,
               SortedSet<Page> pages, Set<Extension> extensions, Set<Theme> themes, Set<I18nResource> i18nResources,
               Configuration configuration,
               String path) {
        this(name, contextPath, pages, extensions, themes, i18nResources, configuration,
             Collections.singletonList(path));
    }

    /**
     * Creates a new app.
     *
     * @param name          name of the app
     * @param contextPath   context path of the app
     * @param pages         pages of the app
     * @param extensions    extensions of the app
     * @param themes        themes of the app
     * @param i18nResources i18n resources of the app
     * @param configuration configurations of the app
     * @param paths         paths to the app
     */
    protected App(String name, String contextPath,
                  SortedSet<Page> pages, Set<Extension> extensions, Set<Theme> themes, Set<I18nResource> i18nResources,
                  Configuration configuration, List<String> paths) {
        this.name = name;
        this.contextPath = contextPath;
        this.pages = pages;
        this.extensions = extensions.stream()
                .collect(Collectors.toMap(ext -> (ext.getType() + ":" + ext.getName()), ext -> ext));
        this.themes = themes.stream()
                .collect(Collectors.toMap(Theme::getName, t -> t));
        this.i18nResources = i18nResources.stream()
                .collect(Collectors.toMap(I18nResource::getLocale, i18nResource -> i18nResource));
        this.configuration = configuration;
        this.paths = paths;
    }

    /**
     * Returns the name of this app.
     *
     * @return name of the app
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the context path of this app.
     *
     * @return context path of the app
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Returns extensions of the given type in this app.
     *
     * @param extensionType type of the extension
     * @return extensions of the given type in the app
     */
    public Set<Extension> getExtensions(String extensionType) {
        return extensions.values().stream()
                .filter(extension -> Objects.equals(extension.getType(), extensionType))
                .collect(Collectors.toSet());
    }

    /**
     * Returns the extension in this app specified by the given type and name.
     *
     * @param extensionType type of the extension
     * @param extensionName name of the extension
     * @return extension in the app
     */
    public Optional<Extension> getExtension(String extensionType, String extensionName) {
        return Optional.ofNullable(extensions.get(extensionType + ":" + extensionName));
    }

    /**
     * Returns the theme in this app specified by the given name.
     *
     * @param themeName name of the theme
     * @return theme in the app
     */
    public Optional<Theme> getTheme(String themeName) {
        return Optional.ofNullable(themes.get(themeName));
    }

    /**
     * Returns the i18n resource in this app specified by the given local.
     *
     * @param locale locale of the i18n resource
     * @return i18n resource in the app
     */
    public Optional<I18nResource> getI18nResource(Locale locale) {
        return Optional.ofNullable(i18nResources.get(locale));
    }

    /**
     * Returns the configurations of this app.
     *
     * @return configurations of the app
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Returns paths that this app can be located.
     *
     * @return paths of the theme
     */
    @Override
    public List<String> getPaths() {
        return paths;
    }

    /**
     * Renders and returns the HTML of the corresponding paage in this app.
     *
     * @param request HTTP request for the page
     * @return HTML content of the page
     * @throws PageNotFoundException if there is no page matching for the HTTP request
     */
    public String renderPage(HttpRequest request) throws PageNotFoundException {
        String uriWithoutContextPath = request.getUriWithoutContextPath();
        Page matchingPage = getMatchingPage(uriWithoutContextPath);
        if (matchingPage != null) {
            return matchingPage.render(request, configuration);
        }

        /* URL correction:
         * See https://googlewebmastercentral.blogspot.com/2010/04/to-slash-or-not-to-slash.html
         * If the tailing '/' is extra or a it is missing, then send 301 with corrected URL.
         */
        String correctedUriWithoutContextPath = (uriWithoutContextPath.endsWith("/") ?
                uriWithoutContextPath.substring(0, (uriWithoutContextPath.length() - 1)) :
                uriWithoutContextPath + "/");
        matchingPage = getMatchingPage(correctedUriWithoutContextPath);
        if (matchingPage != null) {
            // Redirecting to the correct page.
            String correctedUri = request.getContextPath() + correctedUriWithoutContextPath;
            if (request.getQueryString() != null) {
                correctedUri = correctedUri + '?' + request.getQueryString();
            }
            throw new PageRedirectException(correctedUri);
        }

        throw new PageNotFoundException("Requested page '" + uriWithoutContextPath + "' does not exists.");
    }

    private Page getMatchingPage(String uriWithoutContextPath) {
        for (Page page : pages) {
            if (page.matches(uriWithoutContextPath)) {
                return page;
            }
        }
        return null;
    }

    @Override
    public App override(App override) {
        if (!canOverrideBy(override)) {
            throw new IllegalArgumentException(this + " cannot be overridden by " + override + " .");
        }

        return new OverriddenApp(this, override);
    }

    @Override
    public App getBase() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof App)) {
            return false;
        }
        App other = (App) obj;
        return Objects.equals(name, other.name) && Objects.equals(contextPath, other.contextPath) &&
               Objects.equals(paths, other.paths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, contextPath);
    }

    @Override
    public String toString() {
        return "App{name='" + name + "', contextPath='" + contextPath + "', paths=" + paths + "}";
    }

    protected static SortedSet<Page> getPagesOf(App app) {
        return app.pages;
    }

    protected static Collection<Extension> getExtensionsOf(App app) {
        return app.extensions.values();
    }

    protected static Collection<Theme> getThemesOf(App app) {
        return app.themes.values();
    }

    protected static Collection<I18nResource> getI18nResourcesOf(App app) {
        return app.i18nResources.values();
    }
}
