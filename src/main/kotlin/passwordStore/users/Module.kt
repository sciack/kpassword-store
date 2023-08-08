package passwordStore.users

import org.kodein.di.*


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