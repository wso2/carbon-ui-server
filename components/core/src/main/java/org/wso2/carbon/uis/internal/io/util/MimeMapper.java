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

import org.wso2.carbon.uis.internal.exception.ConfigurationException;
import org.wso2.carbon.uis.internal.exception.FileOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * This class lazily loads the 'mime-map.properties' file and maps file extension to mime type using the file.
 *
 * @since 0.8.0
 */
public class MimeMapper {

    private static final String MIME_PROPERTY_FILE = "mime-map.properties";
    private static volatile Properties mimeMap = null;

    private MimeMapper() {
    }

    private static Properties loadMimeMap() throws ConfigurationException, FileOperationException {
        Properties mimeMap = new Properties();
        try (InputStream inputStream = MimeMapper.class.getClassLoader().getResourceAsStream(MIME_PROPERTY_FILE)) {
            if (inputStream == null) {
                throw new FileOperationException("Cannot find MIME types property file '" + MIME_PROPERTY_FILE + "'");
            }
            mimeMap.load(inputStream);
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException(
                    "MIME types property file is '" + MIME_PROPERTY_FILE + "' is invalid.", e);
        } catch (IOException e) {
            throw new FileOperationException("Cannot read MIME types property file '" + MIME_PROPERTY_FILE + "'.", e);
        }
        return mimeMap;
    }

    /**
     * Returns the MIME type for the given file extension.
     *
     * @param extension file extension
     * @return MIME type for the given file extension
     * @throws FileOperationException if cannot find or read the MIME types property file
     * @throws ConfigurationException if MIME types property file is invalid
     */
    public static Optional<String> getMimeType(String extension) throws ConfigurationException, FileOperationException {
        if (mimeMap == null) {
            /* Here, class object 'MimeMapper.class' is used as the synchronization lock because 'getMimeType()' is
            the only is public method. */
            synchronized (MimeMapper.class) {
                if (mimeMap == null) {
                    mimeMap = loadMimeMap();
                }
            }
        }
        return Optional.ofNullable(mimeMap.getProperty(extension));
    }
}
