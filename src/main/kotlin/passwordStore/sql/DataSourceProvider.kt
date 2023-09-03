package passwordStore.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

fun prodDatasource(): HikariDataSource {
    val hikariConfig = HikariConfig("/db.properties")
    val jdbcUrl = System.getProperty(JDBC_URL)
    if (jdbcUrl != null) {
        hikariConfig.jdbcUrl = jdbcUrl
    }
    val password = System.getProperty(DB_PASSWORD)
    if (password != null) {
        hikariConfig.password = password
    }

    return HikariDataSource(hikariConfig)
}

const val JDBC_URL = "jdbc.url"
const val DB_PASSWORD = "dbPassword"

