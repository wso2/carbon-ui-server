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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an i18n resource in a web app.
 *
 * @since 0.12.0
 */
public class I18nResource {

    private final Locale locale;
    private final Map<String, String> messages;

    /**
     * Creates a new i18n resource.
     *
     * @param locale   locale of the i18n resource
     * @param messages messages of the i18n resource
     */
    public I18nResource(Locale locale, Map<String, String> messages) {
        this.locale = locale;
        this.messages = messages;
    }

    /**
     * Returns the locale of this i18n resource.
     *
     * @return locale of the i18n resource
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the formatted message of the given message key. If no message is found for the given message key, then
     * the specified default message will be returned.
     *
     * @param messageKey     key of the message
     * @param messageParams  parameters to format the message, or {@code null} if there are no parameters
     * @param defaultMessage default message, which will be returned if no message is found for the given message key in
     *                       the given locale
     * @return the formatted message or the default message if no message was found for the given message key in the
     * given locale
     */
    public String getMessage(String messageKey, Object[] messageParams, String defaultMessage) {
        String message = messages.get(messageKey);
        if (message == null) {
            return defaultMessage;
        }

        return ((messageParams == null) || (messageParams.length == 0)) ? message :
                new MessageFormat(message, locale).format(messageParams);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof I18nResource)) {
            return false;
        }
        I18nResource other = (I18nResource) obj;
        return Objects.equals(locale, other.locale) && Objects.equals(messages, other.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locale);
    }

    @Override
    public String toString() {
        return "I18nResource{locale=" + locale + "}";
    }

    /**
     * Returns the best matching locale chosen from a set of available locales for the given language ranges.
     *
     * @param languageRanges   a list of comma-separated language ranges or a list of language ranges in the form of the
     *                         "Accept-Language" header defined in
     *                         <a href="https://tools.ietf.org/html/rfc2616#section-14.4">RFC
     *                         2616</a>
     * @param availableLocales available locales to choose from
     * @return Locale the best matching locale, or {@code null} if nothing matches
     */
    public static Locale getMatchingLocale(String languageRanges, Set<Locale> availableLocales) {
        if ((languageRanges == null) || languageRanges.isEmpty()) {
            return null;
        }

        List<Locale> matchingLocales;
        try {
            matchingLocales = Locale.filter(Locale.LanguageRange.parse(languageRanges), availableLocales);
        } catch (IllegalArgumentException e) {
            // languageRanges is ill formed
            return null;
        }
        return matchingLocales.isEmpty() ? null : matchingLocales.get(0);
    }
}
