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

package org.wso2.carbon.uiserver.api.util;

import java.util.List;

/**
 * Represents an entity that can be present in more than one location.
 *
 * @since 0.12.0
 */
public interface Multilocational {

    /**
     * Returns paths that the entity represented this object is present. First path of the returning list has the
     * highest priority and the last path has the least priority.
     *
     * @return paths that the entity represented this object is present
     */
    List<String> getPaths();

    /**
     * Returns the path with the highest priority.
     *
     * @return highest priority path
     */
    default String getHighestPriorityPath() {
        return getPaths().get(0);
    }

    /**
     * Returns the path with the least priority.
     *
     * @return least priority path
     */
    default String getLeastPriorityPath() {
        List<String> paths = getPaths();
        return paths.get(paths.size() - 1);
    }
}
