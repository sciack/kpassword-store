package passwordStore.utils

import kotlinx.datetime.*
import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


fun Clock.currentDateTime(): LocalDateTime = this.now()
    .toLocalDateTime(TimeZone.currentSystemDefault())


val timezone = TimeZone.currentSystemDefault()

fun Timestamp.toKotlinDateTime(): LocalDateTime = this.toLocalDateTime().toKotlinLocalDateTime()

fun LocalDateTime.toTimestamp(timezone: TimeZone): Timestamp = Timestamp.from(this.toInstant(timezone).toJavaInstant())

private val shortFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
fun LocalDateTime.short(): String = this.toJavaLocalDateTime().format(shortFormatter)