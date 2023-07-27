package passwordStore.sql

import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Types
import java.util.*

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



/**
 * Class for simplifying execution of prepared statements.
 */
class Parameters private constructor(
    /**
     * Returns the parsed SQL.
     *
     * @return
     * The parsed SQL.
     */
    val sql: String,
    val keys: Deque<String>
) {

    /**
     * Applies the provided argument values to a prepared statement.
     *
     * @param statement
     * The prepared statement.
     *
     * @param arguments
     * The argument values.
     *
     * @throws SQLException
     * If an exception occurs while applying the argument values.
     */
    @Throws(SQLException::class)
    fun apply(statement: PreparedStatement, arguments: Map<String, Any?>) {
        require(arguments.keys.containsAll(keys)) {
            "Missing value for params: ${keys.subtract(arguments.keys).joinToString(",") { it }}"
        }
        require(keys.containsAll(arguments.keys)) {
            "Provided arguments are more than expected parameters: ${
                arguments.keys.subtract(keys).joinToString(",") { it }
            }"
        }
        var i = 1
        for (key in keys) {
            arguments[key]?.let {
                statement.setObject(i++, it)
            } ?: statement.setNull(i++, Types.NULL)
        }
    }

    companion object {
        private const val EOF = -1

        /**
         * Parses a parameterized SQL statement.
         *
         * @param sql
         * A string containing the SQL to parse.
         *
         * @return
         * A [Parameters] instance containing the parsed SQL.
         */
        fun parse(sql: String?): Parameters {
            requireNotNull(sql)
            return try {
                StringReader(sql).use { sqlReader -> parse(sqlReader) }
            } catch (exception: IOException) {
                throw RuntimeException(exception)
            }

        }

        /**
         * Parses a parameterized SQL statement.
         *
         * @param sqlReader
         * A reader containing the SQL to parse.
         *
         * @return
         * A [Parameters] instance containing the parsed SQL.
         *
         * @throws IOException
         * If an exception occurs while reading the SQL statement.
         */
        @Throws(IOException::class)
        fun parse(sqlReader: Reader?): Parameters {
            requireNotNull(sqlReader)
            val keys: Deque<String> = LinkedList()
            val sqlBuilder = StringBuilder()
            var singleLineComment = false
            var multiLineComment = false
            var quoted = false
            var c = sqlReader.read()
            while (c != EOF) {
                if (c == '-'.code) {
                    sqlBuilder.append(c.toChar())
                    c = sqlReader.read()
                    singleLineComment = c == '-'.code && !multiLineComment
                    sqlBuilder.append(c.toChar())
                    c = sqlReader.read()
                } else if (c == '\r'.code || c == '\n'.code) {
                    sqlBuilder.append(c.toChar())
                    singleLineComment = false
                    c = sqlReader.read()
                } else if (c == '/'.code) {
                    sqlBuilder.append(c.toChar())
                    c = sqlReader.read()
                    multiLineComment = c == '*'.code
                    sqlBuilder.append(c.toChar())
                    c = sqlReader.read()
                } else if (c == '*'.code && multiLineComment) {
                    sqlBuilder.append(c.toChar())
                    c = sqlReader.read()
                    multiLineComment = c != '/'.code
                    sqlBuilder.append(c.toChar())
                    c = sqlReader.read()
                } else if (singleLineComment || multiLineComment) {
                    sqlBuilder.append(c.toChar())
                    c = sqlReader.read()
                } else if (c == ':'.code && !quoted) {
                    c = sqlReader.read()
                    if (c == ':'.code) {
                        sqlBuilder.append("::")
                        c = sqlReader.read()
                    } else {
                        val keyBuilder = StringBuilder()
                        while (c != EOF && Character.isJavaIdentifierPart(c)) {
                            keyBuilder.append(c.toChar())
                            c = sqlReader.read()
                        }
                        keys.add(keyBuilder.toString())
                        sqlBuilder.append("?")
                    }
                } else {
                    if (c == '\''.code) {
                        quoted = !quoted
                    }
                    sqlBuilder.append(c.toChar())
                    c = sqlReader.read()
                }
            }
            return Parameters(sqlBuilder.toString(), keys)
        }
    }
}