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

import org.apache.commons.io.FilenameUtils;
import org.wso2.carbon.uis.internal.exception.FileOperationException;
import org.wso2.carbon.uis.internal.io.util.PathUtils;
import org.wso2.carbon.uis.internal.reference.I18nResourceReference;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

/**
 * A reference to an i18n resource inside a web app artifact.
 *
 * @since 0.8.0
 */
public class ArtifactI18nResourceReference implements I18nResourceReference {

    private final Path propertiesFile;

    /**
     * Creates a reference to the i18n resource specified by the path.
     *
     * @param propertiesFile path to the i18n resource
     */
    public ArtifactI18nResourceReference(Path propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    @Override
    public Locale getLocale() throws FileOperationException {
        String fileName = PathUtils.getName(propertiesFile);
        String languageTag = FilenameUtils.removeExtension(fileName);
        return Locale.forLanguageTag(languageTag);
    }

    @Override
    public Properties getMessages() throws FileOperationException {
        Properties properties = new Properties();
        try {
            properties.load(Files.newBufferedReader(propertiesFile, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new FileOperationException("");
        }
        return properties;
    }
}
