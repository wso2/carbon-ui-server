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

package org.wso2.carbon.uis.internal.io.reference;

import org.wso2.carbon.uis.internal.exception.FileOperationException;
import org.wso2.carbon.uis.internal.io.util.PathUtils;
import org.wso2.carbon.uis.internal.reference.AppReference;
import org.wso2.carbon.uis.internal.reference.ExtensionReference;
import org.wso2.carbon.uis.internal.reference.FileReference;
import org.wso2.carbon.uis.internal.reference.I18nResourceReference;
import org.wso2.carbon.uis.internal.reference.PageReference;
import org.wso2.carbon.uis.internal.reference.ThemeReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A reference to a web app artifact in a directory.
 *
 * @since 1.0.0
 */
public class ArtifactAppReference implements AppReference {

    private final Path appDirectory;

    /**
     * Creates an app reference to the app which resides in the specified directory.
     *
     * @param appDirectory directory that contains the app
     */
    public ArtifactAppReference(Path appDirectory) {
        this.appDirectory = appDirectory.normalize().toAbsolutePath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() throws FileOperationException {
        return PathUtils.getName(appDirectory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<PageReference> getPageReferences() throws FileOperationException {
        Path pages = getPagesDirectory();
        if (!Files.exists(pages)) {
            return Collections.emptySet();
        }
        try {
            return Files.walk(pages)
                    .filter(Files::isRegularFile)
                    .map(pageFile -> new ArtifactPageReference(pageFile, this))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new FileOperationException("An error occurred while listing pages in '" + pages + "'.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ExtensionReference> getExtensionReferences() throws FileOperationException {
        Path extensions = appDirectory.resolve(DIR_NAME_EXTENSIONS);
        if (!Files.exists(extensions)) {
            return Collections.emptySet();
        }
        try {
            return Files.list(extensions)
                    .filter(Files::isDirectory)
                    .flatMap(extensionType -> {
                        try {
                            return Files.list(extensionType)
                                    .filter(Files::isDirectory)
                                    .map(ArtifactExtensionReference::new);
                        } catch (IOException e) {
                            throw new FileOperationException(
                                    "An error occurred while listing extensions in '" + extensionType + "'.", e);
                        }
                    })
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new FileOperationException("An error occurred while listing extension types '" + extensions + "'.",
                                             e);
        }
    }

    @Override
    public Set<ThemeReference> getThemeReferences() throws FileOperationException {
        Path themes = appDirectory.resolve(DIR_NAME_THEMES);
        if (!Files.exists(themes)) {
            return Collections.emptySet();
        }
        try {
            return Files.list(themes)
                    .filter(Files::isDirectory)
                    .map(ArtifactThemeReference::new)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new FileOperationException("An error occurred while listing themes in '" + themes + "'.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<I18nResourceReference> getI18nResourceReferences() throws FileOperationException {
        Path lang = appDirectory.resolve(DIR_NAME_I18N);
        if (!Files.exists(lang)) {
            return Collections.emptySet();
        }
        try {
            return Files.list(lang)
                    .filter(path -> Files.isRegularFile(path) && "properties".equals(PathUtils.getExtension(path)))
                    .map(ArtifactI18nResourceReference::new)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new FileOperationException("An error occurred while listing i18n files in '" + lang + "'.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileReference getConfiguration() throws FileOperationException {
        Path configuration = appDirectory.resolve(FILE_NAME_CONFIGURATION);
        if (Files.exists(configuration)) {
            return new ArtifactFileReference(configuration);
        } else {
            throw new FileOperationException("Cannot find app's configuration file '" + FILE_NAME_CONFIGURATION +
                                             "' in app '" + appDirectory + "'.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() throws FileOperationException {
        return appDirectory.toString();
    }

    Path getPagesDirectory() {
        return appDirectory.resolve(DIR_NAME_PAGES);
    }
}
