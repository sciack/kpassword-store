package passwordStore

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.compose.localDI
import org.kodein.di.compose.rememberInstance
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
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val trayState = rememberTrayState()

    MaterialTheme {

        trayState.sendNotification(Notification("KPasswordStore", "Open app"))
        Scaffold(Modifier.then(Modifier.fillMaxSize()),
            topBar = {
                TopAppBar(navigationIcon = {
                    if (serviceModel.user.id > 0) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            },
                            modifier = Modifier.testTag("Drawer")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "")
                        }
                    }
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
            ModalDrawer(
                drawerState = drawerState,
                drawerContent = {
                    drawer(navController)
                },
                drawerShape = customShape(),
            ) {
                NavigationHost(navController) {
                    composable(Screen.Login) {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        loginPane(loginFunction = { currentUsername, pwd ->
                            submit(di, currentUsername, pwd).onSuccess {
                                user.value = it
                                serviceModel.user = it
                                navController.navigate(Screen.List)
                            }
                        })
                    }

                    authenticatedComposable(Screen.List) {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        serviceModel.fetchAll()
                        servicesTable()
                    }

                    authenticatedComposable(Screen.NewService) {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        newService {
                            coroutineScope.launch(Dispatchers.IO) {
                                serviceModel.store(it).onSuccess {
                                    serviceModel.resetService()
                                    withContext(Dispatchers.Main) {
                                        navController.navigate(Screen.List)
                                    }
                                }

                            }
                        }
                    }

                    authenticatedComposable(Screen.History) {
                        coroutineScope.launch {
                            drawerState.close()
                        }
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
}

typealias LoginFunction = (TextFieldValue, TextFieldValue) -> Result<User>

fun customShape() = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Rectangle(Rect(0f, 0f, 400f, size.height))
    }
}

@Composable
fun drawer(navController: NavController) {
    val serviceViewModel by localDI().instance<ServiceViewModel>()
    Row {
        IconButton(
            onClick = { navController.navigate(Screen.List) },
            modifier = Modifier.testTag("Home").align(Alignment.CenterVertically)
        ) {
            Icon(
                Icons.Default.Home, contentDescription = Screen.List.name,
            )
        }
        Text(
            text = Screen.List.name,
            style = MaterialTheme.typography.h5,
            modifier = Modifier.clickable {
                navController.navigate(Screen.List)
            }.align(Alignment.CenterVertically)
        )
    }
    Spacer(Modifier.height(12.dp))
    Row {
        IconButton(
            onClick = {
                navController.navigate(Screen.NewService)
            },
            modifier = Modifier.testTag("New Service").align(Alignment.CenterVertically)
        ) {
            Icon(
                Icons.Default.Create, contentDescription = Screen.NewService.name,

                )
        }
        Text(
            text = Screen.NewService.name,
            style = MaterialTheme.typography.h5,
            modifier = Modifier.clickable {
                navController.navigate(Screen.NewService)
            }.align(Alignment.CenterVertically)
        )
    }
    Spacer(Modifier.height(12.dp))
    Row {
        IconButton(
            onClick = { navController.navigate(Screen.History) },
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {

            Icon(
                painterResource("/icons/history.svg"),
                contentDescription = Screen.History.name
            )
        }
        Text(
            text = Screen.History.name,
            style = MaterialTheme.typography.h5,
            modifier = Modifier.clickable {
                navController.navigate(Screen.History)
            }.align(Alignment.CenterVertically)
        )
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

    Migration(datasource).migrate()
    application {
        val coroutineScope = rememberCoroutineScope()
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