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

package org.wso2.carbon.uiserver.internal.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.wso2.carbon.uiserver.api.App;
import org.wso2.carbon.uiserver.api.Configuration;
import org.wso2.carbon.uiserver.api.Extension;
import org.wso2.carbon.uiserver.api.I18nResource;
import org.wso2.carbon.uiserver.api.Page;
import org.wso2.carbon.uiserver.api.Theme;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents an overridden web app.
 *
 * @since 0.12.0
 */
public class OverriddenApp extends App {

    private final App base;
    private final App override;

    /**
     * Creates a new overridden app.
     *
     * @param base     base app
     * @param override app that overrides the {@code base} app
     */
    public OverriddenApp(App base, App override) {
        super(override.getName(), override.getContextPath(),
              getPagesFrom(base, override), getExtensionsFrom(base, override), getThemesFrom(base, override),
              getI18nResourcesFrom(base, override), getConfigurationFrom(base, override), getPathsFrom(base, override));
        this.base = base;
        this.override = override;
    }

    @Override
    public App getBase() {
        return base;
    }

    @Override
    public Optional<App> getOverride() {
        return Optional.of(override);
    }

    private static SortedSet<Page> getPagesFrom(App base, App override) {
        SortedSet<Page> pages = new TreeSet<>(getPagesOf(override));
        pages.addAll(getPagesOf(base));
        return pages;
    }

    private static Set<Extension> getExtensionsFrom(App base, App override) {
        Function<Extension, String> getKey = extension -> (extension.getType() + extension.getName());
        Map<String, Extension> extensions = getExtensionsOf(base).stream()
                .collect(Collectors.toMap(getKey, Function.identity()));
        for (Extension extension : getExtensionsOf(override)) {
            extensions.merge(getKey.apply(extension), extension, Extension::override);
        }
        return new HashSet<>(extensions.values());
    }

    private static Set<Theme> getThemesFrom(App base, App override) {
        Map<String, Theme> themes = getThemesOf(base).stream()
                .collect(Collectors.toMap(Theme::getName, Function.identity()));
        for (Theme theme : getThemesOf(override)) {
            themes.merge(theme.getName(), theme, Theme::override);
        }
        return new HashSet<>(themes.values());
    }

    private static Set<I18nResource> getI18nResourcesFrom(App base, App override) {
        return ImmutableSet.<I18nResource>builder()
                .addAll(getI18nResourcesOf(override))
                .addAll(getI18nResourcesOf(base))
                .build();
    }

    private static Configuration getConfigurationFrom(App base, App override) {
        return Objects.equals(override.getConfiguration(), Configuration.DEFAULT_CONFIGURATION) ?
                base.getConfiguration() :
                override.getConfiguration();
    }

    private static List<String> getPathsFrom(App base, App override) {
        return ImmutableList.<String>builder()
                .addAll(override.getPaths())
                .addAll(base.getPaths())
                .build();
    }
}
