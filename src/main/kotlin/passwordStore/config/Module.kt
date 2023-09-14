package passwordStore.config

import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

val configModule = DI.Module("Config") {
    val configFile = configureEnvironment()
    bindProvider {
        ConfigVM(configFile, instance(), instance())
    }
}