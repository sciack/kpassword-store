package passwordStore.sql

import org.flywaydb.core.Flyway
import javax.sql.DataSource

class Migration(private val dataSource: DataSource) {


    fun migrate(clean: Boolean = false) {
        val flyway = Flyway.configure()
            .cleanDisabled(!clean)
            .dataSource(dataSource)
            .load()
        if (clean) {
            flyway.clean()
        }
        flyway.migrate()
    }
}