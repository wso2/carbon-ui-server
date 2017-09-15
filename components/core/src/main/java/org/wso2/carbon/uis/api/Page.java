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

import java.util.Objects;

/**
 * Represents a page of a web app.
 *
 * @since 0.8.0
 */
public class Page implements Comparable<Page> {

    private final UriPatten uriPatten;
    private final String content;

    /**
     * Creates a new page.
     *
     * @param uriPatten URI pattern of the page
     * @param content   content of the page
     */
    public Page(UriPatten uriPatten, String content) {
        this.uriPatten = uriPatten;
        this.content = content;
    }

    /**
     * Returns the URI pattern of this page.
     *
     * @return URI pattern of this page
     */
    public UriPatten getUriPatten() {
        return uriPatten;
    }

    /**
     * Returns the content of this page.
     *
     * @return content of the page
     */
    public String getContent() {
        return content;
    }

    @Override
    public int compareTo(Page otherPage) {
        return (otherPage == null) ? 1 : this.getUriPatten().compareTo(otherPage.getUriPatten());
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj) || ((obj instanceof Page) && (this.compareTo((Page) obj) == 0));
    }

    @Override
    public int hashCode() {
        return Objects.hash(uriPatten, content);
    }

    @Override
    public String toString() {
        return "Page{uriPatten=" + uriPatten + ", content='" + content + "'}";
    }
}
