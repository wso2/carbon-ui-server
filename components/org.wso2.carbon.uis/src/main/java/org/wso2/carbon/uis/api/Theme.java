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

import org.wso2.carbon.uis.api.util.Multilocational;
import org.wso2.carbon.uis.api.util.Overridable;
import org.wso2.carbon.uis.internal.impl.OverriddenTheme;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a theme in a web app.
 *
 * @since 0.8.0
 */
public class Theme implements Multilocational, Overridable<Theme> {

    private final String name;
    private final List<String> paths;

    /**
     * Creates a new theme which can be located in the specified path.
     *
     * @param name name of the theme
     * @param path path to the theme
     */
    public Theme(String name, String path) {
        this(name, Collections.singletonList(path));
    }

    /**
     * Creates a new theme.
     *
     * @param name  name of the theme
     * @param paths paths to the theme
     */
    protected Theme(String name, List<String> paths) {
        this.name = name;
        this.paths = paths;
    }

    /**
     * Returns the name of this theme.
     *
     * @return name of the theme
     */
    public String getName() {
        return name;
    }

    /**
     * Returns paths that this theme can be located.
     *
     * @return paths of the theme
     */
    @Override
    public List<String> getPaths() {
        return paths;
    }

    @Override
    public Theme override(Theme override) {
        if (!canOverrideBy(override)) {
            throw new IllegalArgumentException(this + " cannot be overridden by " + override + " .");
        }
        return new OverriddenTheme(this, override);
    }

    @Override
    public Theme getBase() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Theme)) {
            return false;
        }
        Theme other = (Theme) obj;
        return Objects.equals(name, other.name) && Objects.equals(paths, other.paths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Theme{name='" + name + "', paths=" + paths + "}";
    }
}
