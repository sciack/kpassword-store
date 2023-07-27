package passwordStore.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDateTime

fun Clock.currentTime(): LocalDateTime = this.now()
    .toLocalDateTime(TimeZone.currentSystemDefault())
    .toJavaLocalDateTime()