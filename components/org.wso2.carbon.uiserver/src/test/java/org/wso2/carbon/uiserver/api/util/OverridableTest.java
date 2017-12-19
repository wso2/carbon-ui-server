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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Optional;

/**
 * Test cases for {@link Overridable} interface.
 *
 * @since 0.12.0
 */
public class OverridableTest {

    @Test
    public void testGetOverride() {
        Object override = new Object();
        Overridable<Object> overridable = createOverridable(override);

        Assert.assertTrue(overridable.getOverride().isPresent());
        Assert.assertEquals(overridable.getOverride().orElse(null), override);

        Assert.assertFalse(createOverridable(null).getOverride().isPresent());
    }

    @Test
    public void testHasOverridden() {
        Assert.assertTrue(createOverridable(new Object()).hasOverridden());
        Assert.assertFalse(createOverridable(null).hasOverridden());
    }

    @Test
    public void testHasOverriddenBy() {
        Object override = new Object();
        Overridable<Object> overridable = createOverridable(override);

        Assert.assertTrue(overridable.hasOverriddenBy(override));
        Assert.assertFalse(overridable.hasOverriddenBy(new Object()));
    }

    private static Overridable<Object> createOverridable(Object override) {
        return new OverridableImpl(override);
    }

    /**
     * Implementation of {@link Overridable} for unit tests.
     *
     * @since 0.12.0
     */
    private static class OverridableImpl implements Overridable<Object> {

        private final Object override;

        /**
         * Creates a new instance.
         *
         * @param override override
         */
        public OverridableImpl(Object override) {
            this.override = override;
        }

        @Override
        public Object override(Object override) {
            throw new UnsupportedOperationException("This method is not needed for this unit test.");
        }

        @Override
        public Object getBase() {
            throw new UnsupportedOperationException("This method is not needed for this unit test.");
        }

        @Override
        public Optional<Object> getOverride() {
            return Optional.ofNullable(override);
        }
    }
}
