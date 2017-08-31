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
import org.wso2.carbon.uis.internal.reference.ThemeReference;

import java.nio.file.Path;

/**
 * Created by sajith on 8/29/17.
 */
public class ArtifactThemeReference implements ThemeReference {

    private final Path themeDirectory;

    public ArtifactThemeReference(Path themeDirectory) {
        this.themeDirectory = themeDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() throws FileOperationException {
        return PathUtils.getName(themeDirectory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() throws FileOperationException {
        return themeDirectory.toString();
    }
}
