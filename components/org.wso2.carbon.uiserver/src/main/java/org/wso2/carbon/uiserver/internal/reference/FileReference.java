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

package org.wso2.carbon.uiserver.internal.reference;

import org.wso2.carbon.uiserver.internal.exception.FileOperationException;

/**
 * A reference to a file in a web app.
 *
 * @since 0.8.0
 */
public interface FileReference {

    /**
     * Returns the name of the file represented by this reference.
     *
     * @return name of the file
     * @throws FileOperationException if cannot read file name
     */
    String getName() throws FileOperationException;

    /**
     * Returns the extension of the file represented by this reference.
     *
     * @return extension of the file
     * @throws FileOperationException if cannot read file extension
     */
    String getExtension() throws FileOperationException;

    /**
     * Returns the content of the file represented by this reference.
     *
     * @return content of the file
     * @throws FileOperationException if cannot read file content
     */
    String getContent() throws FileOperationException;

    /**
     * Returns the absolute path to the file represented by this reference.
     *
     * @return absolute path to the file
     * @throws FileOperationException if cannot obtain the path
     */
    String getFilePath() throws FileOperationException;
}
