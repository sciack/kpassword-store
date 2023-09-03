package passwordStore

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import passwordStore.crypto.devCryptExtension
import passwordStore.sql.Migration
import javax.sql.DataSource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes


fun testDatasource(): HikariDataSource {
    val hikariConfig = HikariConfig("/db_test.properties")
    return HikariDataSource(hikariConfig)
}


object DiInjection {
    private val LOGGER = KotlinLogging.logger { }
    val testDi = DI {
        import(devCryptExtension)
        import(diCore())
        bind<Clock> {
            singleton {
                TestClock()
            }
        }
        bind<DataSource>() {
            singleton { testDatasource() }
        }
    }.also {
        LOGGER.warn("Create DI, start db migration")
        val dataSource by it.instance<DataSource>()
        Migration(dataSource).migrate(clean = true)
    }

}

class TestClock(private var time: Instant = Clock.System.now()) : Clock {
    override fun now(): Instant = time

    fun tick(amount: Duration = 1.minutes) {
        time += amount
    }

}
