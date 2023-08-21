package passwordStore

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinLocalDateTime
import passwordStore.services.Service
import passwordStore.users.Roles
import passwordStore.users.User
import java.sql.Timestamp
import java.time.temporal.ChronoUnit


fun testUser(): User = User(
    id = 9999,
    email = "test@example.com",
    fullName = "test user",
    userid = "testUser",
    roles = setOf(Roles.NormalUser),
)


fun testService(service: String = "test") = Service(
    service = service,
    userid = testUser().userid,
    username = "testUser",
    password = "testPwd"
)


fun LocalDateTime.Companion.nowWithMicro(): LocalDateTime =
    java.time.LocalDateTime.now().truncatedTo(ChronoUnit.MICROS).toKotlinLocalDateTime()