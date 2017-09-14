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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Holds the i18n language resources of a web App.
 *
 * @since 0.8.0
 */
public class I18nResources {

    private final Map<Locale, Properties> i18nResources = new HashMap<>();

    /**
     * Adds the given language.
     *
     * @param locale language to be add
     * @param i18n   properties
     */
    public void addI18nResource(Locale locale, Properties i18n) {
        Properties i18nResource = this.i18nResources.get(locale);
        if (i18nResource == null) {
            this.i18nResources.put(locale, i18n);
        } else {
            i18nResource.putAll(i18n);
        }
    }

    /**
     * Returns the best matching locale chosen from the available locales for the given language ranges.
     *
     * @param languageRanges a list of comma-separated language ranges or a list of language ranges in the form of
     *                       the "Accept-Language" header defined in
     *                       <a href="https://tools.ietf.org/html/rfc2616#section-14.4">RFC 2616</a>
     * @return Locale the best matching locale, or {@code null} if nothing matches
     */
    public Locale getLocale(String languageRanges) {
        if ((languageRanges == null) || languageRanges.isEmpty()) {
            return null;
        }

        try {
            return Locale.lookup(Locale.LanguageRange.parse(languageRanges), i18nResources.keySet());
        } catch (IllegalArgumentException e) {
            // languageRanges is ill formed
            return null;
        }
    }

    /**
     * Returns the formatted message of the given message key in the given locale. If no message is found for the
     * given message key in the given locale, then the specified default message will be returned.
     * @param locale locale of the message
     * @param messageKey key of the message
     * @param messageParams parameters to format the message, or {@code null} if there are no parameters
     * @param defaultMessage default message, which will be returned if no message is found for the given message key
     *                       in the given locale
     * @return the formatted message or the default message if no message was found for the given message key in the
     * given locale
     */
    public String getMessage(Locale locale, String messageKey, Object[] messageParams, String defaultMessage) {
        Properties messages = i18nResources.get(locale);
        if (messages == null) {
            return defaultMessage;
        }
        String message = messages.getProperty(messageKey);
        if (message == null) {
            return defaultMessage;
        }

        return ((messageParams == null) || (messageParams.length == 0)) ? message :
                new MessageFormat(message, locale).format(messageParams);
    }
}
