package passwordStore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import passwordStore.services.Service
import passwordStore.users.LocalSetUser
import passwordStore.users.LocalUser
import passwordStore.users.Roles
import passwordStore.users.User
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

@Composable
fun withLogin(currentUser: User?, content: @Composable () -> Unit) {
    val (user, setUser) = remember {
        mutableStateOf(currentUser)
    }
    CompositionLocalProvider(
        LocalUser provides user,
        LocalSetUser provides setUser,
    ) {
        content()
    }
}