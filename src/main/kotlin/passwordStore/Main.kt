package passwordStore

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.compose.localDI
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import passwordStore.sql.Migration
import passwordStore.users.User
import passwordStore.users.UserRepository
import javax.sql.DataSource

@Composable
@Preview
fun App(di: DI) = withDI(di) {
    val username = remember {
        mutableStateOf(TextFieldValue())
    }
    val password = remember {
        mutableStateOf(TextFieldValue())
    }

    val user = remember {
        mutableStateOf<User?>(null)
    }

    val services = remember {
        mutableStateOf<List<Service>>(listOf())
    }

    MaterialTheme {
        if (user.value == null) {
            val di = localDI()
            loginPane(username = username, password = password, loginFunction = { currentUsername, pwd ->
                submit(di, currentUsername, pwd).onSuccess {
                    user.value = it
                }
            })
        } else {
            servicesTable(services.value)
            val servicesRepository by localDI().instance<ServicesRepository>()
            val coroutineScope by localDI().instance<CoroutineScope>()
            coroutineScope.launch(Dispatchers.IO) {
                val fetchedResult = servicesRepository.search(user.value?.userid.orEmpty())
                withContext(Dispatchers.Main) {
                    services.value = fetchedResult
                }
            }

        }
    }
}

typealias LoginFunction = (TextFieldValue, TextFieldValue) -> Result<User>


fun submit(di: DI, username: TextFieldValue, password: TextFieldValue): Result<User> {
    val userRepository by di.instance<UserRepository>()
    LOGGER.info {
        """Username: ${username.text}
        |Password: ${password.text}
    """.trimMargin()
    }
    return runCatching {
        userRepository.login(username.text, password.text)
    }.onFailure {
        LOGGER.warn(it) { "Wrong credentials" }
    }
}

fun main() {
    val di = di()
    val datasource by di.instance<DataSource>()
    Migration(datasource).migrate()
    singleWindowApplication(title = "Password Store") {
        LOGGER.info { "Building the app" }
        App(di)
    }
}


private val LOGGER = KotlinLogging.logger { }