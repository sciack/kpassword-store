package passwordStore

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.compose.localDI
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import passwordStore.config.SetupEnv
import passwordStore.navigation.*
import passwordStore.services.ServiceViewModel
import passwordStore.services.historyTable
import passwordStore.services.newService
import passwordStore.services.servicesTable
import passwordStore.sql.Migration
import passwordStore.users.User
import passwordStore.users.UserRepository
import javax.sql.DataSource

@Composable
@Preview
fun App(di: DI) = withDI(di) {

    val navController by rememberNavController()

    val user = remember {
        mutableStateOf<User?>(null)
    }

    val serviceModel by localDI().instance<ServiceViewModel>()
    val coroutineScope by localDI().instance<CoroutineScope>()

    MaterialTheme {
        Scaffold(Modifier.then(Modifier.fillMaxSize()),
            bottomBar = { bottomBar(navController) },
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
            NavigationHost(navController) {
                composable(Screen.Login) {
                    loginPane(loginFunction = { currentUsername, pwd ->
                        submit(di, currentUsername, pwd).onSuccess {
                            user.value = it
                            serviceModel.user = it
                            navController.navigate(Screen.List)
                        }
                    })
                }

                composable(Screen.List) {
                    serviceModel.fetchAll()
                    servicesTable()
                }

                authenticatedComposable(Screen.NewService) {
                    newService {
                        coroutineScope.launch(Dispatchers.IO) {
                            serviceModel.store(it)
                            serviceModel.resetService()
                            withContext(Dispatchers.Main) {
                                navController.navigate(Screen.List)
                            }
                        }
                    }
                }

                authenticatedComposable(Screen.History) {
                    historyTable(serviceModel.historyEvents.value)
                    if (serviceModel.shouldLoadHistory()) {
                        coroutineScope.launch(Dispatchers.IO) {
                            serviceModel.history("", exactMatch = false)
                        }
                    }
                }

            }.build()


        }
    }
}

typealias LoginFunction = (TextFieldValue, TextFieldValue) -> Result<User>

@Composable
fun bottomBar(navController: NavController) {
    val serviceViewModel by localDI().instance<ServiceViewModel>()
    BottomAppBar(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (navController.currentScreen.value != Screen.Login) {
            BottomNavigation(modifier = Modifier.align(Alignment.Bottom).fillMaxHeight()) {
                BottomNavigationItem(selected = navController.currentScreen.value == Screen.List, icon = {
                    Icon(
                        Icons.Default.Home, contentDescription = Screen.List.name,
                        modifier = Modifier.testTag("Home")
                    )
                }, onClick = {
                    navController.navigate(Screen.List)
                })
                BottomNavigationItem(selected = navController.currentScreen.value == Screen.NewService, icon = {
                    Icon(
                        Icons.Default.Create, contentDescription = Screen.NewService.name,

                        )
                }, onClick = {
                    navController.navigate(Screen.NewService)
                }, modifier = Modifier.testTag("New Service"))
                BottomNavigationItem(selected = navController.currentScreen.value == Screen.History, icon = {
                    Icon(
                        Icons.Default.AccountBox, contentDescription = Screen.History.name,
                        modifier = Modifier.testTag("History")
                    )
                }, onClick = {
                    serviceViewModel.resetHistory()
                    navController.navigate(Screen.History)
                })
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
    SetupEnv.configure(".env")
    val di = di()
    val datasource by di.instance<DataSource>()
    val coroutineScope by di.instance<CoroutineScope>()
    Migration(datasource).migrate()
    application {
        Window(
            onCloseRequest = {
                (datasource as HikariDataSource).close()
                coroutineScope.cancel("Shutdown")
                exitApplication()
            },
            title = "Password Store",
            state = rememberWindowState(width = 1024.dp, height = 768.dp)
        ) {
            LOGGER.info { "Building the app" }
            App(di)
        }
    }
}