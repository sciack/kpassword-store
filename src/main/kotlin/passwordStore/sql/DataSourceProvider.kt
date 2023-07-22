package passwordStore.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

fun prodDatasource(): DataSource {
    val hikariConfig = HikariConfig("/config.properties")
    return HikariDataSource(hikariConfig)
}


fun testDatasource(): DataSource {
    val hikariConfig = HikariConfig("/config_test.properties")
    return HikariDataSource(hikariConfig)
}
