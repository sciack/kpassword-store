package passwordStore.sql

import org.flywaydb.core.Flyway
import javax.sql.DataSource

class Migration(private val dataSource: DataSource) {


    fun migrate(clean: Boolean = false) {
        val flyway = Flyway.configure()
            .cleanDisabled(!clean)
            .baselineVersion("10")
            .baselineOnMigrate(!clean)
            .dataSource(dataSource)
            .loggers("slf4j")
            .load()
        if (clean) {
            flyway.clean()
        }
        //flyway.baseline()
        runCatching {
            flyway.migrate()
        }.recover {
            flyway.repair()
        }
    }
}