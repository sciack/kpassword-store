package passwordStore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import passwordStore.audit.auditModule
import passwordStore.crypto.prodCryptExtension
import passwordStore.sql.prodDatasource
import passwordStore.users.UserRepository
import javax.sql.DataSource

fun di(): DI = DI {
    import(prodCryptExtension)
    import(auditModule)
    bind { singleton {
        CoroutineScope(SupervisorJob())
    } }
    bind<DataSource> {
        singleton {
            prodDatasource()
        }
    }
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