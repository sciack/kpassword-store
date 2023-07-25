package passwordStore

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
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
import passwordStore.audit.Event
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

    val navController by rememberNavController(Screen.Login)
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
        Scaffold(Modifier.padding(16.dp).then(Modifier.fillMaxSize()),
            bottomBar = { bottomBar(navController, navController.currentScreen.value.name) },
            topBar = {
                TopAppBar(navigationIcon = {
                    if (navController.currentScreen.value.allowBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.clickable(onClick = {
                                navController.navigateBack()
                            })
                        )
                    }
                }, title = {
                    Text("Password Store")
                })
            }) {
            Column(modifier = Modifier.fillMaxSize()) {
                NavigationHost(navController) {
                    composable(Screen.Login) {
                        loginPane(loginFunction = { currentUsername, pwd ->
                            submit(di, currentUsername, pwd).onSuccess {
                                user.value = it
                                navController.navigate(Screen.List)
                            }
                        })
                    }

                    composable(Screen.List) {
                        servicesTable(services.value, navController)
                        coroutineScope.launch(Dispatchers.IO) {
                            val fetchedResult = servicesRepository.search(user.value?.userid.orEmpty())
                            withContext(Dispatchers.Main) {
                                services.value = fetchedResult
                            }
                        }
                    }

                    composable(Screen.NewService) {
                        newService(user.value!!) {
                            coroutineScope.launch {
                                servicesRepository.store(it)
                                withContext(Dispatchers.Main) {
                                    navController.navigate(Screen.List)
                                }
                            }
                        }
                    }

                    composable(Screen.Details::class) {
                        val detailsScreen = navController.currentScreen.value as Screen.Details
                        newService(user.value!!, detailsScreen.service) { service ->
                            coroutineScope.launch {
                                if (service.dirty) {
                                    servicesRepository.update(service, service.userid)

                                    withContext(Dispatchers.Main) {
                                        navController.navigate(Screen.List)
                                    }
                                }
                            }
                        }
                    }

                    composable(Screen.History) {
                        val event = remember {
                            mutableStateOf<List<Event>>(listOf())
                        }
                        historyTable(event.value, navController)
                        coroutineScope.launch(Dispatchers.IO) {
                            val fetchedResult = servicesRepository.history("", exactMatch = false, user = user.value!!)
                            withContext(Dispatchers.Main) {
                                event.value = fetchedResult
                            }
                        }
                    }

                }.build()
            }

        }
    }
}

typealias LoginFunction = (TextFieldValue, TextFieldValue) -> Result<User>

@Composable
fun bottomBar(navController: NavController, title: String = "") {
    BottomAppBar(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (navController.currentScreen.value != Screen.Login) {
            BottomNavigation(modifier = Modifier.align(Alignment.Bottom).fillMaxHeight()) {
                BottomNavigationItem(selected = navController.currentScreen.value == Screen.List, icon = {
                    Icon(
                        Icons.Default.Home, contentDescription = Screen.List.name
                    )
                }, onClick = {
                    navController.navigate(Screen.List)
                })
                BottomNavigationItem(selected = navController.currentScreen.value == Screen.NewService, icon = {
                    Icon(
                        Icons.Default.Create, contentDescription = Screen.NewService.name
                    )
                }, onClick = { navController.navigate(Screen.NewService) })
                BottomNavigationItem(selected = navController.currentScreen.value == Screen.History, icon = {
                    Icon(
                        Icons.Default.AccountBox, contentDescription = Screen.History.name
                    )
                }, onClick = { navController.navigate(Screen.History) })
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

private val LOGGER = KotlinLogging.logger { }

fun main() {
    val di = di()
    val datasource by di.instance<DataSource>()
    Migration(datasource).migrate()
    singleWindowApplication(
        title = "Password Store",
        exitProcessOnExit = true,
        resizable = true,

        ) {
        LOGGER.info { "Building the app" }
        App(di)
    }
}