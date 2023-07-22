package passwordStore

import mu.KotlinLogging
import org.junit.jupiter.api.extension.*
import org.kodein.di.*
import passwordStore.sql.Migration
import passwordStore.sql.testDatasource
import javax.sql.DataSource

class DiInjection : ParameterResolver {
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        val parameter = parameterContext.parameter
        return parameter.type == DI::class.java
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        return testDi
    }

    companion object {
        private val LOGGER = KotlinLogging.logger {  }
        private val testDi = DI {
            import(diCore())
            bind<DataSource>() {
                singleton { testDatasource() }
            }
        }.also {
            LOGGER.warn("Create DI, start db migration")
            val dataSource by it.instance<DataSource>()
            Migration(dataSource).migrate(clean = true)
        }
    }
}