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

import com.google.common.collect.ImmutableSet;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collection;

/**
 * Test cases for {@link Mergeable} interface.
 *
 * @since 0.10.5
 */
public class MergeableTest {

    @DataProvider(name = "mergeables")
    public Object[][] getMergeables() {
        return new Object[][]{
                {createMergeable(10), createMergeable(10)},
                {createMergeable(20), createMergeable(20)}
        };
    }

    @DataProvider(name = "unmergeables")
    public Object[][] getUnmergeables() {
        return new Object[][]{
                {createMergeable(10), createMergeable(20)},
                {(Mergeable) other -> null, (Mergeable) other -> null}
        };
    }

    @Test(dataProvider = "mergeables")
    public <T extends Mergeable<T>> void testMergeability(Mergeable<T> mergeable1, Mergeable<T> mergeable2) {
        Assert.assertTrue(mergeable1.isMergeable(mergeable2),
                          mergeable1 + " should be able to merge with " + mergeable2 + ".");
        Assert.assertTrue(mergeable2.isMergeable(mergeable1),
                          mergeable2 + " should be able to merge with " + mergeable1 + ".");
    }

    @Test(dataProvider = "unmergeables")
    public <T extends Mergeable<T>> void testUnmergeability(Mergeable<T> mergeable1, Mergeable<T> mergeable2) {
        Assert.assertFalse(mergeable1.isMergeable(mergeable2),
                           mergeable1 + " shouldn't be able to merge with " + mergeable2 + ".");
        Assert.assertFalse(mergeable2.isMergeable(mergeable1),
                           mergeable2 + " shouldn't be able to merge with " + mergeable1 + ".");
    }

    @Test
    public void testMergeAll() {
        MergeableImpl m1 = createMergeable(10);
        MergeableImpl m2 = createMergeable(20);
        Collection<MergeableImpl> c1 = ImmutableSet.of(m1, m2);

        MergeableImpl m3 = createMergeable(30);
        MergeableImpl m11 = createMergeable(10);
        Collection<MergeableImpl> c2 = ImmutableSet.of(m3, m11);

        Collection<MergeableImpl> merged = Mergeable.mergeAll(c1, c2);
        Assert.assertFalse(merged.contains(m1), m1 + " shouldn't be in the merged collection.");
        Assert.assertFalse(merged.contains(m11), m11 + " shouldn't be in the merged collection.");
        Assert.assertTrue(merged.stream().anyMatch(MergeableImpl::hasMerged),
                          m1 + " and " + m11 + " should be merged.");
        Assert.assertTrue(merged.contains(m2), m2 + " should be in the merged collection.");
        Assert.assertTrue(merged.contains(m3), m3 + " should be in the merged collection.");
        Assert.assertEquals(merged.size(), 3, "Size of the merged collection should be 3.");
    }

    private static MergeableImpl createMergeable(int hash) {
        return new MergeableImpl(hash);
    }

    /**
     * Class that implements {@link Mergeable} interface.
     *
     * @since 0.10.5
     */
    private static class MergeableImpl implements Mergeable<MergeableImpl> {

        private final int hash;
        private final boolean hasMerged;

        /**
         * Creates a new instance.
         *
         * @param hash hash code for the creating object.
         */
        public MergeableImpl(int hash) {
            this(hash, false);
        }

        private MergeableImpl(int hash, boolean hasMerged) {
            this.hash = hash;
            this.hasMerged = hasMerged;
        }

        /**
         * Returns whether this object has been merged with another mergeable object.
         *
         * @return {@code true} if has been merged, otherwise {@code false}
         */
        public boolean hasMerged() {
            return hasMerged;
        }

        @Override
        public MergeableImpl merge(MergeableImpl other) {
            if (!this.isMergeable(other)) {
                throw new IllegalArgumentException(this + " cannot be merged with " + other);
            }
            return new MergeableImpl(other.hash, true);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public String toString() {
            return "Mergeable{hash=" + hash + ", hasMerged=" + hasMerged + '}';
        }
    }
}
