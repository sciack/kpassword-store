package passwordStore

import kotlinx.datetime.*
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import passwordStore.sql.Migration
import passwordStore.sql.testDatasource
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.TemporalAmount
import javax.sql.DataSource

object DiInjection {
    private val LOGGER = KotlinLogging.logger { }
    val testDi = DI {
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

class TestClock(private var time: java.time.LocalDateTime = LocalDateTime.now()) : Clock {
    override fun now(): Instant = time.toKotlinLocalDateTime().toInstant(TimeZone.currentSystemDefault())

    fun tick(amount: TemporalAmount = Duration.ofMinutes(1)) {
        time = time.plus(amount)
    }
}