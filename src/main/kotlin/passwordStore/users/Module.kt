package passwordStore.users

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton


internal val userModule = DI.Module("userModule") {
    bind<UserRepository> {
        singleton {
            UserRepository(instance(), instance())
        }
    }
    bind<UserVM> {
        singleton {
            UserVM(instance(), instance())
        }
    }
}