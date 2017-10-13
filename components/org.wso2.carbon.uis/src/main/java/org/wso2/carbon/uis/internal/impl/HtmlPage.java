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

import org.wso2.carbon.uis.api.Configuration;
import org.wso2.carbon.uis.api.Page;
import org.wso2.carbon.uis.api.UriPatten;
import org.wso2.carbon.uis.api.exception.RenderingException;
import org.wso2.carbon.uis.api.http.HttpRequest;

/**
 * Page based on a HTML file.
 *
 * @since 0.10.3
 */
public class HtmlPage extends Page {

    private final String content;

    /**
     * Creates a new page that renders given HTML content
     *
     * @param uriPatten URI pattern of the page
     * @param content   HTML content of the page
     */
    public HtmlPage(UriPatten uriPatten, String content) {
        super(uriPatten);
        this.content = content;
    }

    @Override
    public String render(HttpRequest request,
                         Configuration configuration) throws RenderingException {
        return content;
    }
}
