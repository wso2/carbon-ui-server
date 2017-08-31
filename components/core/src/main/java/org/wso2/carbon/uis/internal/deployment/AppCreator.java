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

package org.wso2.carbon.uis.internal.deployment;

import org.wso2.carbon.uis.api.App;
import org.wso2.carbon.uis.api.Configuration;
import org.wso2.carbon.uis.api.Extension;
import org.wso2.carbon.uis.api.I18nResources;
import org.wso2.carbon.uis.api.Page;
import org.wso2.carbon.uis.api.Theme;
import org.wso2.carbon.uis.api.UriPatten;
import org.wso2.carbon.uis.internal.reference.AppReference;
import org.wso2.carbon.uis.internal.reference.ExtensionReference;
import org.wso2.carbon.uis.internal.reference.FileReference;
import org.wso2.carbon.uis.internal.reference.PageReference;
import org.wso2.carbon.uis.internal.reference.ThemeReference;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class AppCreator {

    public static App createApp(AppReference appReference, String appContext) {
        SortedSet<Page> pages = createPages(appReference);
        Set<Extension> extensions = appReference.getExtensionReferences().stream()
                .map(AppCreator::createExtension)
                .collect(Collectors.toSet());
        Set<Theme> themes = appReference.getThemeReferences().stream()
                .map(AppCreator::createTheme)
                .collect(Collectors.toSet());
        Configuration configuration = createConfiguration(appReference.getConfiguration());
        I18nResources i18nResources = createI18nResources(appReference);
        return new App(appReference.getName(), appContext, pages, extensions, themes, i18nResources, configuration,
                       appReference.getPath());
    }

    private static SortedSet<Page> createPages(AppReference appReference) {
        List<Page> pages = appReference.getPageReferences().stream()
                .map(AppCreator::createPage)
                .collect(Collectors.toList());
        if ((pages.size() == 1) && (pages.get(0).getUriPatten().matches("/index"))) {
            pages.add(new Page(new UriPatten("/{+index}"), pages.get(0).getContent()));
        }

        return new TreeSet<>(pages);
    }

    private static Page createPage(PageReference pageReference) {
        return new Page(new UriPatten(pageReference.getPathPattern()), pageReference.getHtmlFile().getContent());
    }

    private static Extension createExtension(ExtensionReference extensionReference) {
        return new Extension(extensionReference.getName(), extensionReference.getType(), extensionReference.getPath());
    }

    private static Theme createTheme(ThemeReference themeReference) {
        return new Theme(themeReference.getName(), themeReference.getPath());
    }

    private static I18nResources createI18nResources(AppReference appReference) {
        I18nResources i18nResources = new I18nResources();
        appReference.getI18nResourceReferences()
                .forEach(i18nRef -> i18nResources.addI18nResource(i18nRef.getLocale(), i18nRef.getMessages()));
        return i18nResources;
    }

    private static Configuration createConfiguration(FileReference fileReference) {
        return new Configuration();
    }
}
