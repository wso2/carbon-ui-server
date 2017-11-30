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

package org.wso2.carbon.uis.internal.reference;

import org.wso2.carbon.uis.internal.exception.FileOperationException;

import java.util.Locale;
import java.util.Map;

/**
 * A reference to an i18n resource in a web app artifact.
 *
 * @since 0.8.0
 */
public interface I18nResourceReference {

    /**
     * Returns the locale of the i18n resource represented by this reference.
     *
     * @return locale of the i18n resource
     * @throws FileOperationException if cannot read the locale
     */
    Locale getLocale() throws FileOperationException;

    /**
     * Returns the messages of the i18n resource represented by this reference.
     *
     * @return messages of the i18n resource
     * @throws FileOperationException if cannot read the messages
     */
    Map<String, String> getMessages() throws FileOperationException;
}
