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

package org.wso2.carbon.uis.internal.reference;

import org.wso2.carbon.uis.internal.exception.FileOperationException;

import java.util.Optional;
import java.util.Set;

/**
 * A reference to a web app artifact.
 *
 * @since 0.8.0
 */
public interface AppReference {

    /**
     * Name of the directory that contains pages.
     */
    String DIR_NAME_PAGES = "pages";

    /**
     * Name of the directory that contains themes.
     */
    String DIR_NAME_THEMES = "themes";

    /**
     * Name of the directory that contains extensions.
     */
    String DIR_NAME_EXTENSIONS = "extensions";

    /**
     * Name of the directory that contains i18n resources.
     */
    String DIR_NAME_I18N = "i18n";

    /**
     * Name of the directory that contains public static resources.
     */
    String DIR_NAME_PUBLIC_RESOURCES = "public";

    /**
     * Name of the file that has the app configurations.
     */
    String FILE_NAME_CONFIGURATION = "configuration.yaml";

    /**
     * Returns the name of the app represented by this reference.
     *
     * @return name of the app
     * @throws FileOperationException if cannot read app name
     */
    String getName() throws FileOperationException;

    /**
     * Returns references for the pages that belongs to the app represented by this reference.
     *
     * @return references for pages of this app
     * @throws FileOperationException if cannot find or read pages
     */
    Set<PageReference> getPageReferences() throws FileOperationException;

    /**
     * Returns references for the extensions that belongs to the app represented by this reference.
     *
     * @return references for extensions of this app
     * @throws FileOperationException if cannot find or read extensions
     */
    Set<ExtensionReference> getExtensionReferences() throws FileOperationException;

    /**
     * Returns references for the themes that belongs to the app represented by this reference.
     *
     * @return references for themes of this app
     * @throws FileOperationException if cannot find or read themes
     */
    Set<ThemeReference> getThemeReferences() throws FileOperationException;

    /**
     * Returns references for the i18n resources that belongs to the app represented by this reference.
     *
     * @return references for i18n resources of this app
     * @throws FileOperationException if cannot find or read i18n resources
     */
    Set<I18nResourceReference> getI18nResourceReferences() throws FileOperationException;

    /**
     * Returns a reference to the configuration file of the app represented by this reference.
     *
     * @return if exists a reference to the configuration file of this app, otherwise {@link Optional#empty() empty}.
     */
    Optional<FileReference> getConfiguration() throws FileOperationException;

    /**
     * Returns the absolute path to the app represented by this reference.
     *
     * @return absolute path to the app
     * @throws FileOperationException if cannot obtain the path
     */
    String getPath() throws FileOperationException;
}
