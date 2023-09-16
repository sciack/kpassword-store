package passwordStore.services

import org.kodein.di.*

val services = DI.Module("services") {
    bind<ServicesRepository> {
        singleton {
            ServicesRepository(instance(), instance(), instance())
        }
    }

    bind<ImportSM> {
        singleton {
            ImportSM(instance())
        }
    }

    bindProvider {
        ServicesSM(instance(), instance())
    }

    bindProvider {
        HistorySM(instance())
    }

    bindProvider {
        CreateServiceSM(instance())
    }

    bindProvider {
        ShowServiceSM(instance())
    }
}