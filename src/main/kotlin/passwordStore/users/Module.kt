package passwordStore.users

import org.kodein.di.*


internal val userModule = DI.Module("userModule") {
    bind<UserRepository> {
        singleton {
            UserRepository(instance(), instance())
        }
    }
    bindProvider {
        UserVM(instance())
    }
    bindProvider {
        LoginSM(instance())
    }
    bindProvider {
        CreateUserSM(instance())
    }
}