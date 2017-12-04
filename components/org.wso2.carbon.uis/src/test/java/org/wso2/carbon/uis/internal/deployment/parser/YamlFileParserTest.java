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

package org.wso2.carbon.uis.internal.deployment.parser;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uis.internal.exception.ConfigurationException;
import org.wso2.carbon.uis.internal.exception.FileOperationException;
import org.wso2.carbon.uis.internal.reference.FileReference;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link YamlFileParser} class.
 *
 * @since 0.12.5
 */
public class YamlFileParserTest {

    @Test
    public void testParseWhenCannotReadFile() {
        FileReference fileReference = mock(FileReference.class);
        when(fileReference.getContent()).thenThrow(FileOperationException.class);

        Assert.assertThrows(ConfigurationException.class, () -> YamlFileParser.parse(fileReference, Map.class));
    }

    @Test
    public void testParseInvalidYaml() {
        FileReference fileReference = mock(FileReference.class);
        when(fileReference.getContent()).thenReturn("foo: bar\n -: foobar");

        Assert.assertThrows(ConfigurationException.class, () -> YamlFileParser.parse(fileReference, Map.class));
    }

    @Test
    public void testParseEmptyYaml() {
        FileReference fileReference = mock(FileReference.class);
        when(fileReference.getContent()).thenReturn("# nothing here");

        Assert.assertThrows(ConfigurationException.class, () -> YamlFileParser.parse(fileReference, Map.class));
    }

    @Test
    public void testParse() {
        FileReference fileReference = mock(FileReference.class);
        when(fileReference.getContent()).thenReturn("foo: bar\nfoobar: barz");

        Map map = YamlFileParser.parse(fileReference, Map.class);
        Assert.assertEquals(map.get("foo"), "bar");
        Assert.assertEquals(map.get("foobar"), "barz");
    }
}
