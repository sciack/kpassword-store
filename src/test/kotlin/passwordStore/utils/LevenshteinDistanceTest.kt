package passwordStore.utils


/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import kotlin.test.Test


/**
 * Unit tests for [LevenshteinDistance].
 */
class LevenshteinDistanceTest {
    @Test
    fun testGetLevenshteinDistance_StringString() {
        assertThat(UNLIMITED_DISTANCE.apply("", ""), equalTo(0))
        assertThat(UNLIMITED_DISTANCE.apply("", "a"), equalTo(1))
        assertThat(UNLIMITED_DISTANCE.apply("aaapppp", ""), equalTo(7))
        assertThat(UNLIMITED_DISTANCE.apply("frog", "fog"), equalTo(1))
        assertThat(UNLIMITED_DISTANCE.apply("fly", "ant"), equalTo(3))
        assertThat(UNLIMITED_DISTANCE.apply("elephant", "hippo"), equalTo(7))
        assertThat(UNLIMITED_DISTANCE.apply("hippo", "elephant"), equalTo(7))
        assertThat(UNLIMITED_DISTANCE.apply("hippo", "zzzzzzzz"), equalTo(8))
        assertThat(UNLIMITED_DISTANCE.apply("zzzzzzzz", "hippo"), equalTo(8))
        assertThat(UNLIMITED_DISTANCE.apply("hello", "hallo"), equalTo(1))
    }

    @Test
    fun testGetLevenshteinDistance_StringStringInt() { // empty strings
        assertThat(LevenshteinDistance(0).apply("", ""), equalTo(0))
        assertThat(LevenshteinDistance(8).apply("aaapppp", ""), equalTo(7))
        assertThat(LevenshteinDistance(7).apply("aaapppp", ""), equalTo(7))
        assertThat(LevenshteinDistance(6).apply("aaapppp", ""), equalTo(-1))
        // unequal strings, zero threshold
        assertThat(LevenshteinDistance(0).apply("b", "a"), equalTo(-1))
        assertThat(LevenshteinDistance(0).apply("a", "b"), equalTo(-1))
        // equal strings
        assertThat(LevenshteinDistance(0).apply("aa", "aa"), equalTo(0))
        assertThat(LevenshteinDistance(2).apply("aa", "aa"), equalTo(0))
        // same length
        assertThat(LevenshteinDistance(2).apply("aaa", "bbb"), equalTo(-1))
        assertThat(LevenshteinDistance(3).apply("aaa", "bbb"), equalTo(3))
        // big stripe
        assertThat(LevenshteinDistance(10).apply("aaaaaa", "b"), equalTo(6))
        // distance less than threshold
        assertThat(LevenshteinDistance(8).apply("aaapppp", "b"), equalTo(7))
        assertThat(LevenshteinDistance(4).apply("a", "bbb"), equalTo(3))
        // distance equal to threshold
        assertThat(LevenshteinDistance(7).apply("aaapppp", "b"), equalTo(7))
        assertThat(LevenshteinDistance(3).apply("a", "bbb"), equalTo(3))
        // distance greater than threshold
        assertThat(LevenshteinDistance(2).apply("a", "bbb"), equalTo(-1))
        assertThat(LevenshteinDistance(2).apply("bbb", "a"), equalTo(-1))
        assertThat(LevenshteinDistance(6).apply("aaapppp", "b"), equalTo(-1))
        // stripe runs off array, strings not similar
        assertThat(LevenshteinDistance(1).apply("a", "bbb"), equalTo(-1))
        assertThat(LevenshteinDistance(1).apply("bbb", "a"), equalTo(-1))
        // stripe runs off array, strings are similar
        assertThat(LevenshteinDistance(1).apply("12345", "1234567"), equalTo(-1))
        assertThat(LevenshteinDistance(1).apply("1234567", "12345"), equalTo(-1))
        // old getLevenshteinDistance test cases
        assertThat(LevenshteinDistance(1).apply("frog", "fog"), equalTo(1))
        assertThat(LevenshteinDistance(3).apply("fly", "ant"), equalTo(3))
        assertThat(LevenshteinDistance(7).apply("elephant", "hippo"), equalTo(7))
        assertThat(LevenshteinDistance(6).apply("elephant", "hippo"), equalTo(-1))
        assertThat(LevenshteinDistance(7).apply("hippo", "elephant"), equalTo(7))
        assertThat(LevenshteinDistance(6).apply("hippo", "elephant"), equalTo(-1))
        assertThat(LevenshteinDistance(8).apply("hippo", "zzzzzzzz"), equalTo(8))
        assertThat(LevenshteinDistance(8).apply("zzzzzzzz", "hippo"), equalTo(8))
        assertThat(LevenshteinDistance(1).apply("hello", "hallo"), equalTo(1))
        assertThat(LevenshteinDistance(Int.MAX_VALUE).apply("frog", "fog"), equalTo(1))
        assertThat(LevenshteinDistance(Int.MAX_VALUE).apply("fly", "ant"), equalTo(3))
        assertThat(LevenshteinDistance(Int.MAX_VALUE).apply("elephant", "hippo"), equalTo(7))
        assertThat(LevenshteinDistance(Int.MAX_VALUE).apply("hippo", "elephant"), equalTo(7))
        assertThat(LevenshteinDistance(Int.MAX_VALUE).apply("hippo", "zzzzzzzz"), equalTo(8))
        assertThat(LevenshteinDistance(Int.MAX_VALUE).apply("zzzzzzzz", "hippo"), equalTo(8))
        assertThat(LevenshteinDistance(Int.MAX_VALUE).apply("hello", "hallo"), equalTo(1))
    }

    @Test
    fun testConstructorWithNegativeThreshold() {
        assertThat(
            { LevenshteinDistance(-1) },
            throws<IllegalArgumentException>()
        )
    }

    @Test
    fun testGetThresholdDirectlyAfterObjectInstantiation() {
        assertThat(LevenshteinDistance().threshold, absent())
    }

    companion object {
        private val UNLIMITED_DISTANCE = LevenshteinDistance()
    }
}
