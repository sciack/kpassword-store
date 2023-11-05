package passwordStore

import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.Clock
import org.kodein.di.*
import passwordStore.config.configModule
import passwordStore.crypto.prodCryptExtension
import passwordStore.services.audit.auditModule
import passwordStore.services.services
import passwordStore.sql.Migration
import passwordStore.sql.prodDatasource
import passwordStore.tags.tagModule
import passwordStore.users.userModule
import passwordStore.utils.EventBus

fun diCore(): DI.Module = DI.Module("core") {
    import(auditModule)
    import(services)
    import(tagModule)
    import(userModule)
    bindSingleton {
        EventBus(instance())
    }
    bind {
        singleton {
            CoroutineScope(SupervisorJob() + CoroutineName("KPasswordStore"))
        }
    }
}


fun di() = DI {
    import(configModule)
    import(diCore())
    import(prodCryptExtension)
    bind<Clock> {
        singleton {
            Clock.System
        }
    }
    bind<HikariDataSource> {
        singleton {
            val ds = prodDatasource()
            Migration(ds).migrate()
            ds
        }
    }

}