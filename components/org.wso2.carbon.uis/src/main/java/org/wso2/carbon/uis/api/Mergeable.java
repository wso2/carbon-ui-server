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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Interface representing an object that can be merged with another object to form a new object of the same type.
 *
 * @param <T> the type of the objects that can be merged with this object
 * @since 0.10.5
 */
public interface Mergeable<T extends Mergeable<T>> {

    /**
     * Merges this object and the supplied object, and returns a new "merged" object.
     * <p>
     * Priority is always given to the supplied object. If the same attribute appears in this object as well as in the
     * supplied object, then the "merged" object will have the value from the supplied object.
     *
     * @param other object to merge
     * @return a new object that carries the result of the merge
     * @throws IllegalArgumentException if this {@link #isMergeable(Mergeable) cannot merge} with the other object
     */
    T merge(T  other);

    /**
     * Checks whether this object is mergeable with another object.
     * <p>
     *     Invariants: a.isMergeable(b) == b.isMergeable(a)
     * </p>
     * @param other object to be checked
     * @return {@code true} if can be merged with the supplied object, otherwise {@code false}
     */
    default boolean isMergeable(Mergeable<T> other) {
        return (other != null) && (this.hashCode() == other.hashCode());
    }

    /**
     * Merge all mergeable items in the supplied collections and returns a new collection that contains union.
     *
     * @param c1  collection to be merged
     * @param c2  other collection to be merged (when merging items, priority is given to items in this collection)
     * @param <T> type of the items in the collections
     * @return merged collection
     */
    static <T extends Mergeable<T>> Collection<T> mergeAll(Collection<T> c1, Collection<T> c2) {
        Map<Integer, T> map = new HashMap<>();
        for (T item : c1) {
            map.put(item.hashCode(), item);
        }
        for (T item : c2) {
            // No need to check the items are mergeable before merging since the key of the map is item's hashcode.
            map.merge(item.hashCode(), item, Mergeable::merge);
        }
        return map.values();
    }
}
