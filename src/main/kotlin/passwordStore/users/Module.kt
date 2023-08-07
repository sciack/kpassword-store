package passwordStore.users

import org.kodein.di.*


internal val userModule = DI.Module("userModule") {
    bind<UserRepository> {
        singleton {
            UserRepository(instance())
        }
    }
    bind<UserVM> {
        singleton {
            UserVM(instance(), instance())
        }
    }
}