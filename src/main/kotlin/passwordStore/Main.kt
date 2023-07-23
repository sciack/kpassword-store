package passwordStore

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
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
import passwordStore.navigation.NavController
import passwordStore.navigation.NavigationHost
import passwordStore.navigation.composable
import passwordStore.navigation.rememberNavController
import passwordStore.sql.Migration
import passwordStore.users.User
import passwordStore.users.UserRepository
import javax.sql.DataSource

@Composable
@Preview
fun App(di: DI) = withDI(di) {

    val navController by rememberNavController(Screen.Login.name)
    val currentScreen by remember {
        navController.currentScreen
    }

    val user = remember {
        mutableStateOf<User?>(null)
    }

    val services = remember {
        mutableStateOf<List<Service>>(listOf())
    }
    val servicesRepository by localDI().instance<ServicesRepository>()
    val coroutineScope by localDI().instance<CoroutineScope>()
    MaterialTheme {
        Column(Modifier.padding(16.dp).then(Modifier.fillMaxSize())) {
            topBar(navController, navController.currentScreen.value)
            NavigationHost(navController) {
                composable(Screen.Login.name) {
                    loginPane(loginFunction = { currentUsername, pwd ->
                        submit(di, currentUsername, pwd).onSuccess {
                            user.value = it
                            navController.navigate(Screen.List.name)
                        }
                    })
                }

                composable(Screen.List.name) {
                    servicesTable(services.value)
                    coroutineScope.launch(Dispatchers.IO) {
                        val fetchedResult = servicesRepository.search(user.value?.userid.orEmpty())
                        withContext(Dispatchers.Main) {
                            services.value = fetchedResult
                        }
                    }
                }

                composable(Screen.NewService.name) {
                    newService(user.value!!) {
                        coroutineScope.launch {
                            servicesRepository.store(it)
                            withContext(Dispatchers.Main) {
                                navController.navigate(Screen.List.name)
                            }
                        }
                    }
                }

            }.build()
        }
    }
}

typealias LoginFunction = (TextFieldValue, TextFieldValue) -> Result<User>

@Composable
fun topBar(navController: NavController, title: String = "") {
    BottomAppBar(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (navController.currentScreen.value != Screen.Login.name) {
            BottomNavigation(modifier = Modifier.align(Alignment.Bottom).fillMaxHeight()) {
                BottomNavigationItem(selected = navController.currentScreen.value == Screen.List.name,
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = Screen.List.name
                        )
                    },
                    onClick = {
                        navController.navigate(Screen.List.name)
                    })
                BottomNavigationItem(selected = navController.currentScreen.value == Screen.NewService.name,
                    icon = {
                        Icon(
                            Icons.Default.Create,
                            contentDescription = Screen.NewService.name
                        )
                    },
                    onClick = { navController.navigate(Screen.NewService.name) })
            }
        }

    }
}

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
