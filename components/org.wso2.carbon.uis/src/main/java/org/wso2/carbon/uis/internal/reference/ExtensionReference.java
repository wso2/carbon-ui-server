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

/**
 * A reference to an extension in a web app.
 *
 * @since 0.8.0
 */
public interface ExtensionReference {

    /**
     * Returns the name of the extension represented by this reference.
     *
     * @return name of the extension
     * @throws FileOperationException if cannot read extension name
     */
    String getName() throws FileOperationException;

    /**
     * Returns the type of the extension represented by this reference.
     *
     * @return type of the extension
     * @throws FileOperationException if cannot read extension type
     */
    String getType() throws FileOperationException;

    /**
     * Returns the absolute path to the extension represented by this reference.
     *
     * @return absolute path to the extension
     * @throws FileOperationException if cannot obtain the path
     */
    String getPath() throws FileOperationException;
}
