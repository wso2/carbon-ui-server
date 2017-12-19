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

import com.google.common.collect.ImmutableList;
import org.wso2.carbon.uis.api.Theme;

import java.util.Optional;

/**
 * Represents an overridden theme.
 *
 * @since 0.12.0
 */
public class OverriddenTheme extends Theme {

    private final Theme base;
    private final Theme override;

    /**
     * Creates a new overridden theme.
     *
     * @param base     base theme
     * @param override theme that overrides the {@code base} theme
     */
    public OverriddenTheme(Theme base, Theme override) {
        super(override.getName(),
              ImmutableList.<String>builder().addAll(override.getPaths()).addAll(base.getPaths()).build());
        this.base = base;
        this.override = override;
    }

    @Override
    public Theme getBase() {
        return base;
    }

    @Override
    public Optional<Theme> getOverride() {
        return Optional.of(override);
    }
}
