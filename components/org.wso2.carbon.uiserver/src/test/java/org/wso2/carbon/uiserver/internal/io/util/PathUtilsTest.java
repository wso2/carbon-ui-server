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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link PathUtils} class.
 *
 * @since 0.12.5
 */
public class PathUtilsTest {

    @Test
    public void testGetNameReturnsEmptyString() {
        Path filePath = mock(Path.class);
        when(filePath.getFileName()).thenReturn(null);
        Assert.assertEquals(PathUtils.getName(filePath), "");
    }

    @Test
    public void testGetName() {
        Path filePath = Paths.get("test/some-file.txt");
        Assert.assertEquals(PathUtils.getName(filePath), "some-file.txt");
    }

    @Test
    public void testGetExtension() {
        Path filePath = Paths.get("test/some-file.txt");
        Assert.assertEquals(PathUtils.getExtension(filePath), "txt");
    }
}
