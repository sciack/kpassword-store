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
package passwordStore.utils

import java.util.*

/**
 * An algorithm for measuring the difference between two character sequences.
 *
 *
 *
 * This is the number of changes needed to change one sequence into another,
 * where each change is a single character modification (deletion, insertion
 * or substitution).
 *
 *
 *
 *
 * This code has been adapted from Apache Commons Lang 3.3.
 *
 *
 * @since 1.0
 */
class LevenshteinDistance @JvmOverloads constructor(threshold: Int? = null)  {
    /**
     * Gets the distance threshold.
     *
     * @return The distance threshold
     */
    /**
     * Threshold.
     */
    val threshold: Int?

    /**
     *
     * Find the Levenshtein distance between two Strings.
     *
     *
     * A higher score indicates a greater distance.
     *
     *
     * The previous implementation of the Levenshtein distance algorithm
     * was from [http://www.merriampark.com/ld.htm](http://www.merriampark.com/ld.htm)
     *
     *
     * Chas Emerick has written an implementation in Java, which avoids an OutOfMemoryError
     * which can occur when my Java implementation is used with very large strings.<br></br>
     * This implementation of the Levenshtein distance algorithm
     * is from [http://www.merriampark.com/ldjava.htm](http://www.merriampark.com/ldjava.htm)
     *
     * <pre>
     * distance.apply(null, *)             = IllegalArgumentException
     * distance.apply(*, null)             = IllegalArgumentException
     * distance.apply("","")               = 0
     * distance.apply("","a")              = 1
     * distance.apply("aaapppp", "")       = 7
     * distance.apply("frog", "fog")       = 1
     * distance.apply("fly", "ant")        = 3
     * distance.apply("elephant", "hippo") = 7
     * distance.apply("hippo", "elephant") = 7
     * distance.apply("hippo", "zzzzzzzz") = 8
     * distance.apply("hello", "hallo")    = 1
    </pre> *
     *
     * @param left the first string, must not be null
     * @param right the second string, must not be null
     * @return result distance, or -1
     * @throws IllegalArgumentException if either String input `null`
     */
    fun apply(left: CharSequence, right: CharSequence): Int {
        return threshold?.let { limitedCompare(left, right, it) } ?: unlimitedCompare(left, right)
    }

    companion object {
        /**
         * Gets the default instance.
         *
         * @return The default instance
         */
        /**
         * Default instance.
         */
        val defaultInstance = LevenshteinDistance()

        /**
         * Find the Levenshtein distance between two CharSequences if it's less than or
         * equal to a given threshold.
         *
         *
         *
         * This implementation follows from Algorithms on Strings, Trees and
         * Sequences by Dan Gusfield and Chas Emerick's implementation of the
         * Levenshtein distance algorithm from [http://www.merriampark.com/ld.htm](http://www.merriampark.com/ld.htm)
         *
         *
         * <pre>
         * limitedCompare(null, *, *)             = IllegalArgumentException
         * limitedCompare(*, null, *)             = IllegalArgumentException
         * limitedCompare(*, *, -1)               = IllegalArgumentException
         * limitedCompare("","", 0)               = 0
         * limitedCompare("aaapppp", "", 8)       = 7
         * limitedCompare("aaapppp", "", 7)       = 7
         * limitedCompare("aaapppp", "", 6))      = -1
         * limitedCompare("elephant", "hippo", 7) = 7
         * limitedCompare("elephant", "hippo", 6) = -1
         * limitedCompare("hippo", "elephant", 7) = 7
         * limitedCompare("hippo", "elephant", 6) = -1
        </pre> *
         *
         * @param left the first CharSequence, must not be null
         * @param right the second CharSequence, must not be null
         * @param threshold the target threshold, must not be negative
         * @return result distance, or -1
         */
        private fun limitedCompare(leftWord: CharSequence?, rightWord: CharSequence?, threshold: Int): Int { // NOPMD
            require(!(leftWord == null || rightWord == null)) { "CharSequences must not be null" }
            require(threshold >= 0) { "Threshold must not be negative" }
            var left: CharSequence = leftWord
            var right: CharSequence = rightWord

            /*
         * This implementation only computes the distance if it's less than or
         * equal to the threshold value, returning -1 if it's greater. The
         * advantage is performance: unbounded distance is O(nm), but a bound of
         * k allows us to reduce it to O(km) time by only computing a diagonal
         * stripe of width 2k + 1 of the cost table. It is also possible to use
         * this to compute the unbounded Levenshtein distance by starting the
         * threshold at 1 and doubling each time until the distance is found;
         * this is O(dm), where d is the distance.
         *
         * One subtlety comes from needing to ignore entries on the border of
         * our stripe eg. p[] = |#|#|#|* d[] = *|#|#|#| We must ignore the entry
         * to the left of the leftmost member We must ignore the entry above the
         * rightmost member
         *
         * Another subtlety comes from our stripe running off the matrix if the
         * strings aren't of the same size. Since string s is always swapped to
         * be the shorter of the two, the stripe will always run off to the
         * upper right instead of the lower left of the matrix.
         *
         * As a concrete example, suppose s is of length 5, t is of length 7,
         * and our threshold is 1. In this case we're going to walk a stripe of
         * length 3. The matrix would look like so:
         *
         * <pre>
         *    1 2 3 4 5
         * 1 |#|#| | | |
         * 2 |#|#|#| | |
         * 3 | |#|#|#| |
         * 4 | | |#|#|#|
         * 5 | | | |#|#|
         * 6 | | | | |#|
         * 7 | | | | | |
         * </pre>
         *
         * Note how the stripe leads off the table as there is no possible way
         * to turn a string of length 5 into one of length 7 in edit distance of
         * 1.
         *
         * Additionally, this implementation decreases memory usage by using two
         * single-dimensional arrays and swapping them back and forth instead of
         * allocating an entire n by m matrix. This requires a few minor
         * changes, such as immediately returning when it's detected that the
         * stripe has run off the matrix and initially filling the arrays with
         * large values so that entries we don't compute are ignored.
         *
         * See Algorithms on Strings, Trees and Sequences by Dan Gusfield for
         * some discussion.
         */
            var n = left.length // length of left
            var m = right.length // length of right
            // if one string is empty, the edit distance is necessarily the length
// of the other
            if (n == 0) {
                return if (m <= threshold) m else -1
            } else if (m == 0) {
                return if (n <= threshold) n else -1
            }
            if (n > m) { // swap the two strings to consume less memory
                val tmp: CharSequence = left
                left = right
                right = tmp
                n = m
                m = right.length
            }
            // the edit distance cannot be less than the length difference
            if (m - n > threshold) {
                return -1
            }
            var p = IntArray(n + 1) // 'previous' cost array, horizontally
            var d = IntArray(n + 1) // cost array, horizontally
            var tempD: IntArray // placeholder to assist in swapping p and d
            // fill in starting table values
            val boundary = Math.min(n, threshold) + 1
            for (i in 0 until boundary) {
                p[i] = i
            }
            // these fills ensure that the value above the rightmost entry of our
// stripe will be ignored in following loop iterations
            Arrays.fill(p, boundary, p.size, Int.MAX_VALUE)
            Arrays.fill(d, Int.MAX_VALUE)
            // iterates through t
            for (j in 1..m) {
                val rightJ = right[j - 1] // jth character of right
                d[0] = j
                // compute stripe indices, constrain to array size
                val min = Math.max(1, j - threshold)
                val max = if (j > Int.MAX_VALUE - threshold) n else Math.min(
                        n, j + threshold)
                // ignore entry left of leftmost
                if (min > 1) {
                    d[min - 1] = Int.MAX_VALUE
                }
                // iterates through [min, max] in s
                for (i in min..max) {
                    if (left[i - 1] == rightJ) { // diagonally left and up
                        d[i] = p[i - 1]
                    } else { // 1 + minimum of cell to the left, to the top, diagonally
// left and up
                        d[i] = 1 + Math.min(Math.min(d[i - 1], p[i]), p[i - 1])
                    }
                }
                // copy current distance counts to 'previous row' distance counts
                tempD = p
                p = d
                d = tempD
            }
            // if p[n] is greater than the threshold, there's no guarantee on it
// being the correct
// distance
            return if (p[n] <= threshold) {
                p[n]
            } else -1
        }

        /**
         *
         * Find the Levenshtein distance between two Strings.
         *
         *
         * A higher score indicates a greater distance.
         *
         *
         * The previous implementation of the Levenshtein distance algorithm
         * was from [
 * https://web.archive.org/web/20120526085419/http://www.merriampark.com/ldjava.htm](https://web.archive.org/web/20120526085419/http://www.merriampark.com/ldjava.htm)
         *
         *
         * This implementation only need one single-dimensional arrays of length s.length() + 1
         *
         * <pre>
         * unlimitedCompare(null, *)             = IllegalArgumentException
         * unlimitedCompare(*, null)             = IllegalArgumentException
         * unlimitedCompare("","")               = 0
         * unlimitedCompare("","a")              = 1
         * unlimitedCompare("aaapppp", "")       = 7
         * unlimitedCompare("frog", "fog")       = 1
         * unlimitedCompare("fly", "ant")        = 3
         * unlimitedCompare("elephant", "hippo") = 7
         * unlimitedCompare("hippo", "elephant") = 7
         * unlimitedCompare("hippo", "zzzzzzzz") = 8
         * unlimitedCompare("hello", "hallo")    = 1
        </pre> *
         *
         * @param left the first CharSequence, must not be null
         * @param right the second CharSequence, must not be null
         * @return result distance, or -1
         * @throws IllegalArgumentException if either CharSequence input is `null`
         */
        private fun unlimitedCompare(leftWord: CharSequence?, rightWord: CharSequence?): Int {
            require(!(leftWord == null || rightWord == null)) { "CharSequences must not be null" }
            var left: CharSequence = leftWord
            var right: CharSequence = rightWord

            /*
           This implementation use two variable to record the previous cost counts,
           So this implementation use less memory than previous impl.
         */
            var n = left.length // length of left
            var m = right.length // length of right
            if (n == 0) {
                return m
            } else if (m == 0) {
                return n
            }
            if (n > m) { // swap the input strings to consume less memory
                val tmp: CharSequence = left
                left = right
                right = tmp
                n = m
                m = right.length
            }
            val p = IntArray(n + 1)
            // indexes into strings left and right
            var i: Int // iterates through left
            var j: Int // iterates through right
            var upperLeft: Int
            var upper: Int
            var rightJ: Char // jth character of right
            var cost: Int // cost
            i = 0
            while (i <= n) {
                p[i] = i
                i++
            }
            j = 1
            while (j <= m) {
                upperLeft = p[0]
                rightJ = right[j - 1]
                p[0] = j
                i = 1
                while (i <= n) {
                    upper = p[i]
                    cost = if (left[i - 1] == rightJ) 0 else 1
                    // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                    p[i] = Math.min(Math.min(p[i - 1] + 1, p[i] + 1), upperLeft + cost)
                    upperLeft = upper
                    i++
                }
                j++
            }
            return p[n]
        }
    }
    /**
     *
     *
     * If the threshold is not null, distance calculations will be limited to a maximum length.
     * If the threshold is null, the unlimited version of the algorithm will be used.
     *
     *
     * @param threshold
     * If this is null then distances calculations will not be limited.
     * This may not be negative.
     */
    /**
     *
     *
     * This returns the default instance that uses a version
     * of the algorithm that does not use a threshold parameter.
     *
     *
     * @see LevenshteinDistance.getDefaultInstance
     */
    init {
        require(!(threshold != null && threshold < 0)) { "Threshold must not be negative" }
        this.threshold = threshold
    }
}

