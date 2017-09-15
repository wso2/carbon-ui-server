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

import org.wso2.carbon.uis.api.http.HttpRequest;
import org.wso2.carbon.uis.internal.exception.PageNotFoundException;

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
public class App {

    private final String name;
    private final String contextPath;
    private final SortedSet<Page> pages;
    private final Map<String, Extension> extensions;
    private final Map<String, Theme> themes;
    private final I18nResources i18nResources;
    private final Configuration configuration;
    private final String path;

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
               SortedSet<Page> pages, Set<Extension> extensions, Set<Theme> themes,
               I18nResources i18nResources, Configuration configuration,
               String path) {
        this.name = name;
        this.contextPath = contextPath;
        this.pages = pages;
        this.extensions = extensions.stream()
                .collect(Collectors.toMap(ext -> (ext.getType() + ":" + ext.getName()), ext -> ext));
        this.themes = themes.stream()
                .collect(Collectors.toMap(Theme::getName, t -> t));
        this.i18nResources = i18nResources;
        this.configuration = configuration;
        this.path = path;
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
     * Returns i18n resources of this app.
     *
     * @return i18n resources of the app
     */
    public I18nResources getI18nResources() {
        return i18nResources;
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
     * Returns the path of this app.
     *
     * @return path of the app
     */
    public String getPath() {
        return path;
    }

    /**
     * Renders and returns the HTML of the corresponding paage in this app.
     *
     * @param request HTTP request for the page
     * @return HTML content of the page
     * @throws PageNotFoundException if there is no page matching for the HTTP request
     */
    public String renderPage(HttpRequest request) throws PageNotFoundException {
        for (Page page : pages) {
            if (page.getUriPatten().matches(request.getUriWithoutContextPath())) {
                return page.getContent();
            }
        }

        throw new PageNotFoundException("Requested page '" + request.getUriWithoutContextPath() + "' does not exists.");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof App)) {
            return false;
        }
        App app = (App) obj;
        return Objects.equals(name, app.name) && Objects.equals(contextPath, app.contextPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, contextPath);
    }

    @Override
    public String toString() {
        return "App{name='" + name + "', contextPath='" + contextPath + "', path='" + path + "'}";
    }
}
