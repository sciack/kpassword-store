package passwordStore.services.audit

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import passwordStore.services.Service

data class Event(
    val service: Service, val action: Action, val actionDate: LocalDateTime = Clock.System.now().toLocalDateTime(
        TimeZone.currentSystemDefault()
    )
)

data class AuditMessage(val event: Event)

@Suppress("EnumEntryName")
enum class Action { delete, insert, update }