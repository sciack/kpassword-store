package passwordStore.sql

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import io.mockk.mockk
import io.mockk.verifyAll

import java.lang.IllegalArgumentException
import java.sql.PreparedStatement
import java.sql.Types
import kotlin.test.Test
import kotlin.test.assertEquals

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class ParametersTest {
    @Test
    fun testParameters() {
        val parameters = Parameters.parse("insert into xyz (foo, bar) values :foo, :bar")
        assertEquals("insert into xyz (foo, bar) values ?, ?", parameters.sql)
        assertThat(parameters.keys.toList(), equalTo(listOf("foo", "bar")))
    }

    @Test
    fun testColon() {
        val parameters = Parameters.parse("select * from xyz where foo = 'a:b:c'")
        assertEquals("select * from xyz where foo = 'a:b:c'", parameters.sql)
    }

    @Test
    fun testDoubleColon() {
        val parameters = Parameters.parse("select 'ab:c'::varchar(16) as abc")
        assertEquals("select 'ab:c'::varchar(16) as abc", parameters.sql)
    }

    @Test
    fun testSingleLineComment() {
        val parameters = Parameters.parse("-- this is a comment: hello\r\nselect * from xyz where foo = :foo")
        assertEquals(
            "-- this is a comment: hello\r\nselect * from xyz where foo = ?",
            parameters.sql
        )
    }

    @Test
    fun testMultiLineComment() {
        val parameters =
            Parameters.parse("/* this is a comment: hello\r\nand so is this: goodbye */ select * from xyz where foo = :foo")
        assertEquals(
            "/* this is a comment: hello\r\nand so is this: goodbye */ select * from xyz where foo = ?",
            parameters.sql
        )
    }

    @Test
    fun testSingleAndMultiLineComment() {
        val parameters =
            Parameters.parse("/* this is a comment: hello -- and so is this: goodbye */ select * from xyz where foo = :foo")
        assertEquals(
            "/* this is a comment: hello -- and so is this: goodbye */ select * from xyz where foo = ?",
            parameters.sql
        )
    }

    @Test
    fun `should apply the parameters`() {
        val parameters = Parameters.parse("insert into xyz (foo, bar) values :foo, :bar")
        val ps = mockk<PreparedStatement>(relaxed = true)
        val input = mapOf("foo" to "foos", "bar" to "bars")
        parameters.apply(ps, input)
        verifyAll {
            ps.setObject(1, input["foo"])
            ps.setObject(2, input["bar"])
        }
    }

    @Test
    fun `should apply Null parameters`() {
        val parameters = Parameters.parse("insert into xyz (foo, bar) values :foo, :bar")
        val ps = mockk<PreparedStatement>(relaxed = true)
        val input = mapOf("foo" to null, "bar" to "bars")
        parameters.apply(ps, input)
        verifyAll {
            ps.setNull(1, Types.NULL)
            ps.setObject(2, input["bar"])
        }
    }

    @Test
    fun `should throw an error if not enough parameter are passed`() {
        val parameters = Parameters.parse("insert into xyz (foo, bar) values :foo, :bar")
        val ps = mockk<PreparedStatement>(relaxed = true)
        val input = mapOf( "bar" to "bars")
        assertThat( {
            parameters.apply(ps, input)
        }, throws<IllegalArgumentException>() )
    }


    @Test
    fun `should throw an error when too many parameter are passed`() {
        val parameters = Parameters.parse("insert into xyz (foo, bar) values :bar")
        val ps = mockk<PreparedStatement>(relaxed = true)
        val input = mapOf( "bar" to "bars", "foo" to "foos")
        assertThat( {
            parameters.apply(ps, input)
        }, throws<IllegalArgumentException>() )
    }
}