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

package org.wso2.carbon.uiserver.internal.impl;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.Template;
import org.wso2.carbon.uiserver.api.Configuration;
import org.wso2.carbon.uiserver.api.Page;
import org.wso2.carbon.uiserver.api.UriPatten;
import org.wso2.carbon.uiserver.api.exception.RenderingException;
import org.wso2.carbon.uiserver.api.http.HttpRequest;
import org.wso2.carbon.uiserver.internal.exception.AppCreationException;
import org.wso2.carbon.uiserver.internal.exception.FileOperationException;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Page based on a Handlebars template.
 *
 * @since 0.10.3
 */
public class HbsPage extends Page {

    private static final Handlebars HANDLEBARS = new Handlebars();

    private final Template template;

    /**
     * Creates a new page based on a Handlebars template.
     *
     * @param uriPatten URI pattern of the page
     * @param template  Handlebars template
     */
    public HbsPage(UriPatten uriPatten, String template) {
        super(uriPatten);
        this.template = compile(template);
    }

    @Override
    public String render(HttpRequest request, Configuration configuration) throws RenderingException {
        Map<String, String> model = Collections.singletonMap("@contextPath", request.getContextPath());
        Context context = Context.newContext(model);
        try {
            return template.apply(context);
        } catch (IOException e) {
            throw new RenderingException("Cannot load page Handlebars template.", e);
        } catch (HandlebarsException e) {
            throw new RenderingException("Cannot render page Handlebars template.", e);
        }
    }

    private static Template compile(String template) {
        try {
            return HANDLEBARS.compileInline(template);
        } catch (IOException e) {
            throw new FileOperationException("Cannot load Handlebars template.", e);
        } catch (HandlebarsException e) {
            throw new AppCreationException("Cannot compile Handlebars template.", e);
        }
    }
}
