package passwordStore.sql

import org.flywaydb.core.Flyway
import passwordStore.LOGGER
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
        }.recoverCatching {
            LOGGER.warn(it) {
                "Failed migration, try to repair and execute again"
            }
            flyway.repair()
            flyway.migrate()
        }.getOrThrow()
    }

    companion object {
    }

}