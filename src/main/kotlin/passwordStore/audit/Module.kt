package passwordStore.audit

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton


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
    bind<AuditEventDeque>{
        singleton {
            AuditEventDeque(instance(), instance())
        }
    }
}