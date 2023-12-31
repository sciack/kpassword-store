package passwordStore.services.audit

import org.kodein.di.DI
import org.kodein.di.bindEagerSingleton
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import passwordStore.utils.EventBus


internal val auditModule = DI.Module("auditModule") {
    bindSingleton {
        AuditRepository(instance(), instance())
    }
    bindEagerSingleton {
        AuditEventDeque(instance(), instance())
    }
}