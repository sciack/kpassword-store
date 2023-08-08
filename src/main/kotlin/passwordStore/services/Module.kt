package passwordStore.services

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton

val services = DI.Module("services") {
    bind<ServicesRepository> {
        singleton {
            ServicesRepository(instance(), instance(), instance())
        }
    }
    bind {
        singleton {
            ServiceVM(instance(), instance(), instance())
        }
    }

}