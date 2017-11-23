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

import com.google.common.collect.ImmutableSet;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/**
 * Test cases for {@link I18nResource} class.
 *
 * @since 0.12.0
 */
public class I18nResourceTest {

    @Test
    public void testGetMessage() {
        Properties properties = new Properties();
        properties.put("welcome", "Hello!");
        properties.put("welcome-name", "Hello {0}!");
        I18nResource i18nResource = new I18nResource(Locale.US, properties);

        Assert.assertEquals(i18nResource.getMessage("bye", null, "Bye!"), "Bye!");
        Assert.assertEquals(i18nResource.getMessage("welcome", null, null), "Hello!");
        Assert.assertEquals(i18nResource.getMessage("welcome-name", new Object[]{"Alice"}, null), "Hello Alice!");
    }

    @Test
    public void testGetMatchingLocale() {
        Set<Locale> availableLocales = ImmutableSet.of(Locale.FRENCH, Locale.US, Locale.CANADA);

        Assert.assertEquals(I18nResource.getMatchingLocale(null, availableLocales), null);
        Assert.assertEquals(I18nResource.getMatchingLocale("", availableLocales), null);
        Assert.assertEquals(I18nResource.getMatchingLocale("foo", availableLocales), null);

        Assert.assertEquals(I18nResource.getMatchingLocale("en", availableLocales), Locale.US);
        Assert.assertEquals(I18nResource.getMatchingLocale("en-US", availableLocales), Locale.US);
        Assert.assertEquals(I18nResource.getMatchingLocale("fr, en;q=0.9, en-GB;q=0.8, en-US;q=0.7",
                                                           availableLocales), Locale.FRENCH);
        Assert.assertEquals(I18nResource.getMatchingLocale("si, en;q=0.9, en-GB;q=0.8, en-US;q=0.7, en-CA;q=0.5",
                                                           availableLocales), Locale.US);
    }

    @DataProvider
    public Object[][] equalI18nResources() {
        I18nResource i18nResource = new I18nResource(Locale.US, null);
        Properties messages1 = new Properties();
        messages1.put("hello", "Welcome!");
        Properties messages2 = new Properties();
        messages2.put("hello", "Welcome!");
        return new Object[][]{
                {i18nResource, i18nResource},
                {new I18nResource(Locale.US, null), new I18nResource(Locale.US, null)},
                {new I18nResource(Locale.US, new Properties()), new I18nResource(Locale.US, new Properties())},
                {new I18nResource(Locale.US, messages1), new I18nResource(Locale.US, messages2)}
        };
    }

    @Test(dataProvider = "equalI18nResources")
    public void testEqual(I18nResource i18nResource1, I18nResource i18nResource2) {
        Assert.assertEquals(i18nResource1, i18nResource2);
    }
}
