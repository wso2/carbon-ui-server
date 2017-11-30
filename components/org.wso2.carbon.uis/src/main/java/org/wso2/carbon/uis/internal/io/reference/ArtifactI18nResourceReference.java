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

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.wso2.carbon.uis.internal.exception.FileOperationException;
import org.wso2.carbon.uis.internal.io.util.PathUtils;
import org.wso2.carbon.uis.internal.reference.I18nResourceReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/**
 * A reference to an i18n resource inside a web app artifact.
 *
 * @since 0.8.0
 */
public class ArtifactI18nResourceReference implements I18nResourceReference {

    public static final String I18N_RESOURCE_FILE_EXTENSION = "json";
    private static final Gson GSON = new Gson();
    private static final Type GSON_TYPE = new TypeToken<Map<String, String>>() { }.getType();

    private final Path messagesFile;

    /**
     * Creates a reference to the i18n resource specified by the path.
     *
     * @param messagesFile path to the i18n resource
     */
    public ArtifactI18nResourceReference(Path messagesFile) {
        this.messagesFile = messagesFile;
    }

    @Override
    public Locale getLocale() throws FileOperationException {
        return Locale.forLanguageTag(PathUtils.getExtension(messagesFile));
    }

    @Override
    public Map<String, String> getMessages() throws FileOperationException {
        try {
            BufferedReader bufferedReader = Files.newBufferedReader(messagesFile, StandardCharsets.UTF_8);
            return GSON.fromJson(bufferedReader, GSON_TYPE);
        } catch (IOException e) {
            throw new FileOperationException("Cannot read content of I18n message file '" + messagesFile + "'.");
        } catch (JsonParseException e) {
            throw new FileOperationException("I18n message file '" + messagesFile + "' is not a valid JSON.");
        }
    }
}
