package passwordStore.services

import org.kodein.di.*

val services = DI.Module("services") {
    bind<ServicesRepository> {
        singleton {
            ServicesRepository(instance(), instance(), instance())
        }
    }
    bind {
        singleton {
            ServiceVM(instance(), instance())
        }
    }

    bindProvider {
        HistorySM(instance())
    }
}