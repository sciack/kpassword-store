package passwordStore.utils

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.greaterThanOrEqualTo
import com.natpryce.hamkrest.lessThan
import kotlin.test.Test
import kotlin.test.assertEquals

internal class StringDistanceTest {

    @Test
    fun `same string should return 1`() {
        val distance = "Test string".distance( "Test string")
        assertEquals(1.0, distance)
    }

    @Test
    fun `should get mispelling`() {
        val distance = "Test string".distance( "Tets sting")
        println("distance is $distance")
        assertThat(distance, greaterThanOrEqualTo(0.5))

    }

    @Test
    fun `should get mispelling on short string`() {
        val distance = "HMRC".distance("hrmc")
        println("distance is $distance")
        assertThat(distance, greaterThanOrEqualTo(.5))
    }

    @Test
    fun `should not match complete different string`() {
        val distance = "should never match".distance( "test service")
        println("distance is $distance")
        assertThat(distance, lessThan(.3))
    }


}