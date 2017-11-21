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

package org.wso2.carbon.uis.api.util;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents an entity that can be overridden by another entity .
 *
 * @param <T> the type of the objects that can be use to override this object
 * @since 0.12.0
 */
public interface Overridable<T> {

    /**
     * Overrides this object (base) by the supplied object (override) and returns a new object that represents the
     * override.
     *
     * @param override the object that overrides this object
     * @return new object that represents the override
     * @throws IllegalArgumentException if this object cannot be overridden the the supplied object
     */
    T override(T override);

    /**
     * Returns the base object of the override.
     *
     * @return {@code this} if has not been overridden, otherwise the base object
     */
    T getBase();

    /**
     * Returns the overriding object of the override.
     *
     * @return the overriding object of has been overridden, otherwise {@link Optional#empty() empty}
     */
    default Optional<T> getOverride() {
        return Optional.empty();
    }

    /**
     * Returns whether this object has been overridden by some other object.
     *
     * @return {@code true} if has been overridden, otherwise {@code false}
     */
    default boolean hasOverridden() {
        return this.getOverride().isPresent();
    }

    /**
     * Returns whether this object has been overridden by the specified object.
     *
     * @param other other object to be checked
     * @return {@code true} if has been overridden by the supplied object, otherwise {@code false}
     */
    default boolean hasOverriddenBy(T other) {
        return this.getOverride()
                .map(override -> Objects.equals(other, override))
                .orElse(false);
    }

    /**
     * Returns whether this object can be overridden by the specified object.
     *
     * @param other other object to be checked
     * @return {@code true} if can be overridden, otherwise {@code false}
     */
    default boolean canOverrideBy(T other) {
        return (other != null) && (this.hashCode() == other.hashCode());
    }
}
