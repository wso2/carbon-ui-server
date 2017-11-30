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

package org.wso2.carbon.uis.internal.io.util;

import org.wso2.carbon.uis.api.exception.UISRuntimeException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

/**
 * Maps MIME types to file extensions.
 *
 * @since 0.8.0
 */
public class MimeMapper {

    private static final String FILE_NAME_MIME_TYPES = "mime-types.yaml";
    private static Map<String, String> mimeTypes;

    private MimeMapper() {
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> loadMimeTypes() throws UISRuntimeException {
        try (InputStream inputStream = MimeMapper.class.getClassLoader().getResourceAsStream(FILE_NAME_MIME_TYPES)) {
            if (inputStream == null) {
                throw new UISRuntimeException(
                        "Cannot find MIME types file '" + FILE_NAME_MIME_TYPES + "' in class path.");
            }
            return new Yaml().loadAs(inputStream, Map.class);
        } catch (IOException e) {
            throw new UISRuntimeException("Cannot read MIME types file '" + FILE_NAME_MIME_TYPES + "'.", e);
        } catch (Exception e) {
            throw new UISRuntimeException("MIME types file is '" + FILE_NAME_MIME_TYPES + "' is invalid.", e);
        }
    }

    /**
     * Returns the MIME type for the given file extension.
     *
     * @param extension file extension
     * @return MIME type for the given file extension
     * @throws UISRuntimeException if cannot find or read the MIME types file, or it is invalid
     */
    public static Optional<String> getMimeType(String extension) throws UISRuntimeException {
        if (mimeTypes == null) {
            synchronized (MimeMapper.class) {
                if (mimeTypes == null) {
                    mimeTypes = loadMimeTypes();
                }
            }
        }
        return Optional.ofNullable(mimeTypes.get(extension));
    }
}
