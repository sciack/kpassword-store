package passwordStore.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

fun prodDatasource(): DataSource {
    val hikariConfig = HikariConfig("/config.properties")
    val jdbcUrl = System.getProperty("jdbc.url")
    if (jdbcUrl != null) {
        hikariConfig.jdbcUrl = jdbcUrl
    }
    return HikariDataSource(hikariConfig)
}


fun testDatasource(): DataSource {
    val hikariConfig = HikariConfig("/config_test.properties")
    return HikariDataSource(hikariConfig)
}
