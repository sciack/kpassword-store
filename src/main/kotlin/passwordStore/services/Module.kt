package passwordStore.services

import org.kodein.di.*

val services = DI.Module("services") {
    bind<ServicesRepository> {
        singleton {
            ServicesRepository(instance(), instance(), instance())
        }
    }
    bindProvider {
        ServiceSM(instance(), instance())
    }

    bindProvider {
        HistorySM(instance())
    }

    bindProvider {
        CreateServiceSM(instance())
    }
}