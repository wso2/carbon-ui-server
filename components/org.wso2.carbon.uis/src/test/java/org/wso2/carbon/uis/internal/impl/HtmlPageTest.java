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

package org.wso2.carbon.uis.internal.impl;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uis.api.UriPatten;

/**
 * Test cases for {@link HtmlPage} class.
 *
 * @since 0.12.5
 */
public class HtmlPageTest {

    @Test
    public void testRender() {
        String pageContent = "<p>some html</p>";
        HtmlPage page = new HtmlPage(new UriPatten("/"), pageContent);
        Assert.assertEquals(page.render(null, null), pageContent);
    }
}
