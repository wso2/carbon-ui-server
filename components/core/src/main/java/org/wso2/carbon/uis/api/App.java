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
 * Created by sajith on 8/22/17.
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

    public String getName() {
        return name;
    }

    public String getContextPath() {
        return contextPath;
    }

    public Set<Extension> getExtensions(String extensionType) {
        return extensions.values().stream()
                .filter(extension -> extension.getType().equals(extensionType))
                .collect(Collectors.toSet());
    }

    public Optional<Extension> getExtension(String extensionType, String extensionName) {
        return Optional.ofNullable(extensions.get(extensionType + ":" + extensionName));
    }

    public Optional<Theme> getTheme(String themeName) {
        return Optional.ofNullable(themes.get(themeName));
    }

    public I18nResources getI18nResources() {
        return i18nResources;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getPath() {
        return path;
    }

    public String renderPage(HttpRequest request) {
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
        App otherApp = (App) obj;
        return Objects.equals(name, otherApp.name) && Objects.equals(contextPath, otherApp.contextPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, contextPath);
    }

    @Override
    public String toString() {
        return "App{" + "name='" + name + '\'' + ", contextPath='" + contextPath + '\'' + '}';
    }
}
