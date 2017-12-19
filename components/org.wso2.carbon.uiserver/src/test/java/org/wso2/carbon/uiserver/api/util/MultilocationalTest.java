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

import com.google.common.collect.ImmutableList;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Test cases for {@link Multilocational} interface.
 *
 * @since 0.12.0
 */
public class MultilocationalTest {

    @Test
    public void testGetPaths() {
        ImmutableList<String> paths = ImmutableList.of("p1", "p2");
        Multilocational multilocational = createMultilocational(paths);

        Assert.assertEquals(multilocational.getPaths(), paths);
    }

    @Test
    public void testGetHighestPriorityPath() {
        ImmutableList<String> paths = ImmutableList.of("p1", "p2");
        Multilocational multilocational = createMultilocational(paths);

        Assert.assertEquals(multilocational.getHighestPriorityPath(), "p1");
    }

    @Test
    public void testGetLeastPriorityPath() {
        ImmutableList<String> paths = ImmutableList.of("p1", "p2");
        Multilocational multilocational = createMultilocational(paths);

        Assert.assertEquals(multilocational.getLeastPriorityPath(), "p2");
    }

    private static Multilocational createMultilocational(List<String> paths) {
        return new MultilocationalImpl(paths);
    }

    /**
     * Implementation of {@link Multilocational} for unit tests.
     *
     * @since 0.12.0
     */
    private static class MultilocationalImpl implements Multilocational {

        private final List<String> paths;

        /**
         * Creates a new instance.
         *
         * @param paths paths
         */
        public MultilocationalImpl(List<String> paths) {
            this.paths = paths;
        }

        @Override
        public List<String> getPaths() {
            return paths;
        }
    }
}
