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
import passwordStore.navigation.navigation
import passwordStore.services.ServiceViewModel
import passwordStore.services.ServicesRepository
import passwordStore.sql.prodDatasource
import passwordStore.tags.tagModule
import passwordStore.users.UserRepository
import javax.sql.DataSource

fun diCore(): DI.Module = DI.Module("core") {

    import(auditModule)
    import(repositories)
    import(navigation)
    import(tagModule)
    bind {
        singleton {
            CoroutineScope(SupervisorJob())
        }
    }

    bind {
        singleton {
            ServiceViewModel(instance(), instance(), instance())
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

fun di() = DI {
    import(diCore())
    import(prodCryptExtension)
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