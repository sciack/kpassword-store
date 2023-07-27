/*
 *
 * MIT License
 *
 * Copyright (c) 2020 Mirko Sciachero
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package passwordStore.sql

import mu.KotlinLogging
import passwordStore.sql.Parameters.Companion.parse
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import javax.sql.DataSource

typealias Mapper<T> = (ResultSet) -> T

fun <T> DataSource.query(sql: String, params: Map<String, Any?>, mapper: Mapper<T>): List<T> {
    return this.connection.use { c ->
        val parameters = parse(sql)
        c.prepareStatement(parameters.sql).use { ps ->
            parameters.apply(ps, params)
            ps.query(mapper)
        }
    }
}

fun <T> DataSource.query(sql: String, vararg params: Any, mapper: Mapper<T>): List<T> {
    return this.connection.use { c ->
        c.prepareStatement(sql).use { ps ->
            for ((index, param) in params.withIndex()) {
                ps.setObject(index + 1, param)
            }
            ps.query(mapper)
        }
    }
}


fun <T> DataSource.singleRow(sql: String, params: Map<String, Any?>, mapper: Mapper<T>): T =
    this.connection.use { c ->
        c.singleRow(sql, params, mapper)
    }


fun <T> Connection.singleRow(sql: String, params: Map<String, Any?>, mapper: Mapper<T>): T {

    val parameters = parse(sql)
    return this.prepareStatement(parameters.sql).use { ps ->
        parameters.apply(ps, params)
        ps.singleRowExecution(mapper)
    }
}


fun <T> DataSource.singleRow(sql: String, vararg params: Any, mapper: Mapper<T>): T =
    connection.use { c ->
        c.prepareStatement(sql).use { ps ->
            for ((index, param) in params.withIndex()) {
                ps.setObject(index + 1, param)
            }
            ps.singleRowExecution(mapper)
        }
    }


/**
 * By default the code commit, if you don't need a commit maybe more parameter is needed
 */
fun DataSource.saveOrUpdate(sql: String, vararg params: Any?): Int {
    return this.connection.use { c ->
        c.prepareStatement(sql).use { ps ->
            params.withIndex().forEach { (index, param) ->
                if (param == null) {
                    ps.setNull(index + 1, Types.NULL)
                } else {
                    ps.setObject(index + 1, param)
                }
            }
            ps.executeUpdate()
        }
    }

}

fun Connection.saveOrUpdate(sql: String, vararg params: Any?): Int {
    return this.prepareStatement(sql).use { ps ->
        params.withIndex().forEach { (index, param) ->
            if (param == null) {
                ps.setNull(index + 1, Types.NULL)
            } else {
                ps.setObject(index + 1, param)
            }
        }
        ps.executeUpdate()

    }

}

fun Connection.saveOrUpdate(sql: String, params: Map<String, Any?>): Int {
    val parameters = parse(sql)
    return prepareStatement(parameters.sql).use { ps ->
        parameters.apply(ps, params)
        ps.executeUpdate()
    }
}


fun <T> PreparedStatement.singleRowExecution(
    mapper: Mapper<T>
): T {
    executeQuery().use { rs ->
        check(rs.next()) {
            "Empty result set"
        }
        val result = mapper(rs)
        check(!rs.next()) {
            "More than one row returned"
        }
        return result
    }
}

private fun <T> PreparedStatement.query(mapper: Mapper<T>): List<T> = executeQuery().use { rs ->
    val result = mutableListOf<T>()
    while (rs.next()) {
        result.add(mapper(rs))
    }
    result
}


fun DataSource.performTransaction(tx: Connection.() -> Unit) {
    this.connection.use { c ->
        val isAutocommit = c.autoCommit
        runCatching {
            c.autoCommit = false
            c.tx()
        }.onSuccess {
            LOGGER.debug { "Transaction successful, start commit" }
            runCatching {
                c.commit()
            }.onFailure {
                LOGGER.warn { "Fail to commit" }
            }
            LOGGER.debug { "Transaction successful, commit done" }
            c.autoCommit = isAutocommit
        }.onFailure {
            LOGGER.warn(it) { "Error in transaction" }
            runCatching {
                c.rollback()
            }.onFailure {
                LOGGER.warn { "Fail to rollback" }
            }
            c.autoCommit = isAutocommit
        }.getOrThrow()
    }

}

private val LOGGER = KotlinLogging.logger { }