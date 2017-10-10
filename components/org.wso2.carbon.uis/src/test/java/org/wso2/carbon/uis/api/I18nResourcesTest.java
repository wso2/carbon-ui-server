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

package org.wso2.carbon.uis.api;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Locale;
import java.util.Properties;

/**
 * Test cases for {@link I18nResources} class.
 *
 * @since 0.10.0
 */
public class I18nResourcesTest {

    @Test
    public void testGetLocale() {
        I18nResources i18nResources = new I18nResources();
        i18nResources.addI18nResource(Locale.FRENCH, null);
        i18nResources.addI18nResource(Locale.US, null);
        i18nResources.addI18nResource(Locale.CANADA, null);

        Assert.assertEquals(i18nResources.getLocale(null), null);
        Assert.assertEquals(i18nResources.getLocale(""), null);
        Assert.assertEquals(i18nResources.getLocale("foo"), null);

        Assert.assertEquals(i18nResources.getLocale("en"), Locale.US);
        Assert.assertEquals(i18nResources.getLocale("en-US"), Locale.US);
        Assert.assertEquals(i18nResources.getLocale("fr, en;q=0.9, en-GB;q=0.8, en-US;q=0.7"), Locale.FRENCH);
        Assert.assertEquals(i18nResources.getLocale("si, en;q=0.9, en-GB;q=0.8, en-US;q=0.7, en-CA;q=0.5"), Locale.US);
    }

    @Test
    public void testGetMessage() {
        I18nResources i18nResources = new I18nResources();
        Properties properties = new Properties();
        properties.put("welcome", "Hello!");
        properties.put("welcome-name", "Hello {0}!");
        i18nResources.addI18nResource(Locale.US, properties);

        Assert.assertEquals(i18nResources.getMessage(Locale.FRENCH, null, null, "Bonjour"), "Bonjour");
        Assert.assertEquals(i18nResources.getMessage(Locale.US, "bye", null, "Bye!"), "Bye!");
        Assert.assertEquals(i18nResources.getMessage(Locale.US, "welcome", null, null), "Hello!");
        Assert.assertEquals(i18nResources.getMessage(Locale.US, "welcome-name", new Object[]{"Alice"}, null),
                            "Hello Alice!");
    }
}
