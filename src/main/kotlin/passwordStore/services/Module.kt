package passwordStore.services

import org.kodein.di.*

val services = DI.Module("services") {
    bind<ServicesRepository> {
        singleton {
            ServicesRepository(instance(), instance(), instance())
        }
    }

    bindProvider {
        ExportSM(instance())
    }


    bindProvider {
        ImportSM(instance())
    }

    bindProvider {
        ServicesSM(instance(), instance(), instance(), instance())
    }

    bindProvider {
        HistorySM(instance())
    }

    bindProvider {
        CreateServiceSM(instance())
    }

    bindProvider {
        ServiceSM(instance())
    }
}