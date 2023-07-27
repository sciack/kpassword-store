package passwordStore.audit

import org.kodein.di.*


internal val auditModule = DI.Module("auditModule") {
    bind<AuditRepository> {
        singleton {
            AuditRepository(instance(), instance())
        }
    }
    bind<EventBus> {
        singleton {
            EventBus(instance())
        }
    }
    bind<AuditEventDeque> {
        eagerSingleton {
            AuditEventDeque(instance(), instance())
        }
    }
}