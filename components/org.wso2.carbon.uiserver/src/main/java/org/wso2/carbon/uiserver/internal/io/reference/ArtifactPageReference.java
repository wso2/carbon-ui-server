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

package org.wso2.carbon.uiserver.internal.io.reference;

import org.apache.commons.io.FilenameUtils;
import org.wso2.carbon.uiserver.internal.exception.FileOperationException;
import org.wso2.carbon.uiserver.internal.reference.FileReference;
import org.wso2.carbon.uiserver.internal.reference.PageReference;

import java.nio.file.Path;

/**
 * A reference to a page inside a web app artifact.
 *
 * @since 0.8.0
 */
public class ArtifactPageReference implements PageReference {

    private final Path pageFile;
    private final ArtifactAppReference appReference;

    /**
     * Creates a reference to the page specified by the path.
     *
     * @param pageFile     path to the page
     * @param appReference reference to the belonging app
     */
    public ArtifactPageReference(Path pageFile, ArtifactAppReference appReference) {
        this.pageFile = pageFile;
        this.appReference = appReference;
    }

    @Override
    public String getPathPattern() throws FileOperationException {
        StringBuilder sb = new StringBuilder();
        Path pagesDirectory = appReference.getPagesDirectory().relativize(pageFile);
        for (Path path : pagesDirectory) {
            sb.append('/').append(FilenameUtils.removeExtension(path.toString()));
        }
        return sb.toString();
    }

    @Override
    public FileReference getHtmlFile() {
        return new ArtifactFileReference(pageFile);
    }
}
