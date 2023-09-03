package passwordStore

import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.Clock
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import passwordStore.audit.auditModule
import passwordStore.config.ConfigVM
import passwordStore.crypto.prodCryptExtension
import passwordStore.navigation.navigation
import passwordStore.services.services
import passwordStore.sql.prodDatasource
import passwordStore.tags.tagModule
import passwordStore.users.userModule
import java.nio.file.Path
import javax.sql.DataSource

fun diCore(): DI.Module = DI.Module("core") {

    import(auditModule)
    import(services)
    import(navigation)
    import(tagModule)
    import(userModule)
    bind {
        singleton {
            CoroutineScope(SupervisorJob() + CoroutineName("KPasswordStore"))
        }
    }
}


fun di(configFile: Path) = DI {
    import(diCore())
    import(prodCryptExtension)
    bind<Clock> {
        singleton {
            Clock.System
        }
    }
    bind<HikariDataSource> {
        singleton {
            prodDatasource()
        }
    }
    bind<ConfigVM> {
        singleton {
            ConfigVM(configFile, instance(), instance())
        }
    }
}