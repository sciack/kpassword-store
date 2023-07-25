package passwordStore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.Clock
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import passwordStore.audit.auditModule
import passwordStore.crypto.prodCryptExtension
import passwordStore.sql.prodDatasource
import passwordStore.users.UserRepository
import javax.sql.DataSource

fun diCore(): DI.Module = DI.Module("core") {
    import(prodCryptExtension)
    import(auditModule)
    import(repositories)
    bind { singleton {
        CoroutineScope(SupervisorJob())
    } }

    bind {
        singleton {
            Services(instance(), instance())
        }
    }

}

fun di() = DI {
    import(diCore())
    bind<Clock> {
        singleton {
            Clock.System
        }
    }
    bind<DataSource> {
        singleton {
            prodDatasource()
        }
    }
}
val repositories = DI.Module("repositories") {
    bind<UserRepository> {
        singleton {
            UserRepository(instance())
        }
    }
    bind<ServicesRepository> {
        singleton {
            ServicesRepository(instance(), instance(), instance())
        }
    }
}