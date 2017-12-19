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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for {@link MimeMapper} class.
 *
 * @since 0.12.5
 */
public class MimeMapperTest {

    @Test
    public void testGetMimeType() {
        Assert.assertTrue(MimeMapper.getMimeType("jpeg").isPresent());
        Assert.assertEquals(MimeMapper.getMimeType("jpeg").orElse(null), "image/jpeg");
        Assert.assertFalse(MimeMapper.getMimeType("foo").isPresent());
    }
}
