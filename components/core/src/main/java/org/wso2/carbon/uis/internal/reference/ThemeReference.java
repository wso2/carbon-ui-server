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
 * A reference to a theme in a web app artifact.
 *
 * @since 0.8.0
 */
public interface ThemeReference {

    /**
     * Returns the name of the theme represented by this reference.
     *
     * @return name of the theme
     * @throws FileOperationException if cannot read theme name
     */
    String getName() throws FileOperationException;

    /**
     * Returns the absolute path to the theme represented by this reference.
     *
     * @return absolute path to the theme
     * @throws FileOperationException if cannot obtain the path
     */
    String getPath() throws FileOperationException;
}
