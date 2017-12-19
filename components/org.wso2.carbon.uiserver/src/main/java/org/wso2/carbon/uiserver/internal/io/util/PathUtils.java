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

package org.wso2.carbon.uiserver.internal.io.util;

import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;

/**
 * Utility methods for {@link Path}s.
 *
 * @since 0.8.0
 */
public class PathUtils {

    private PathUtils() {
    }

    /**
     * Returns the file name of the given path.
     *
     * @param path file/directory path
     * @return file name (never {@code null})
     */
    public static String getName(Path path) {
        Path fileName = path.getFileName();
        return (fileName == null) ? "" : fileName.toString();
    }

    /**
     * Returns the file extension of the given path.
     *
     * @param filePath file path
     * @return file extension (never {@code null})
     */
    public static String getExtension(Path filePath) {
        return FilenameUtils.getExtension(filePath.getFileName().toString());
    }
}
