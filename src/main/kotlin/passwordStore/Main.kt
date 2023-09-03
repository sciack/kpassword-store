package passwordStore

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import mu.KLogger
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.compose.localDI
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import org.slf4j.bridge.SLF4JBridgeHandler
import passwordStore.config.configureEnvironment
import passwordStore.config.getMode
import passwordStore.navigation.NavController
import passwordStore.navigation.rememberNavController
import passwordStore.services.download
import passwordStore.services.upload
import passwordStore.sql.Migration
import passwordStore.users.UserVM
import passwordStore.utils.StatusHolder
import javax.sql.DataSource
import kotlin.system.exitProcess

@Composable
@Preview
fun app(di: DI) = withDI(di) {

    val navController by rememberNavController()

    val userVM by rememberInstance<UserVM>()
    val coroutineScope = rememberCoroutineScope()
    StatusHolder.scaffoldState = rememberScaffoldState()

    val currentUser = remember {
        userVM.loggedUser
    }

    navController.onSelection {
        coroutineScope.launch(Dispatchers.Main) {
            StatusHolder.closeDrawer()
        }
    }

    MaterialTheme {
        Scaffold(Modifier.then(Modifier.fillMaxSize()),
            scaffoldState = StatusHolder.scaffoldState,
            topBar = {
                TopAppBar(navigationIcon = {
                    if (currentUser.value.id > 0) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    if (StatusHolder.scaffoldState.drawerState.isClosed) {
                                        StatusHolder.openDrawer()
                                    } else {
                                        StatusHolder.closeDrawer()
                                    }
                                }
                            },
                            modifier = Modifier.testTag("Drawer")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "")
                        }
                    }
                    if (navController.currentScreen.value.allowBack) {
                        IconButton(
                            onClick = { navController.navigateBack() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }, title = {
                    Spacer(modifier = Modifier.width(24.dp))
                    Text("Password Store - ${getMode()}")
                    Spacer(modifier = Modifier.width(24.dp))
                    if (currentUser.value.id > 0) {
                        Text(currentUser.value.fullName)
                    }
                }, actions = {
                    if (currentUser.value.id > 0)
                        IconButton(
                            onClick = {
                                navController.navigate(Screen.Settings)
                            },
                        ) {
                            Icon(Icons.Default.Settings, "Settings")
                        }
                })
            }) {

            ModalDrawer(
                drawerState = StatusHolder.scaffoldState.drawerState,
                drawerContent = {
                    drawer(navController)
                },
                drawerShape = customShape(),
            ) {
                route(navController)
            }
        }
    }
}

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
fun RowScope.MenuItem(
    icon: @Composable () -> Unit,
    title: String,
    testTag: String = "",
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.testTag(testTag).align(Alignment.CenterVertically).size(24.dp)
    ) {
        icon()
    }
    Text(
        text = title,
        style = MaterialTheme.typography.body1,
        modifier = Modifier.clickable {
            onClick()
        }.align(Alignment.CenterVertically)
    )
}


@Composable
fun drawer(navController: NavController) {
    val di: DI = localDI()
    val coroutineScope = rememberCoroutineScope()
    val userVM by rememberInstance<UserVM>()

    Row {
        MenuItem(
            onClick = { navController.navigate(Screen.List) },
            title = Screen.List.name,
            testTag = "Home",
            icon = {
                Icon(
                    Icons.Default.Home, contentDescription = Screen.List.name,
                )
            }
        )
    }
    Spacer(Modifier.height(12.dp))
    Row {
        MenuItem(
            onClick = { navController.navigate(Screen.NewService) },
            title = Screen.NewService.name,
            testTag = "New Service",
            icon = {
                Icon(
                    Icons.Default.Create, contentDescription = Screen.NewService.name,
                )
            }
        )
    }
    Spacer(Modifier.height(12.dp))
    Row {
        MenuItem(
            onClick = { navController.navigate(Screen.History) },
            title = Screen.History.name,
            testTag = "History",
            icon = {
                Icon(
                    painterResource("/icons/history.svg"),
                    contentDescription = Screen.History.name
                )
            }
        )
    }
    Spacer(Modifier.height(12.dp))
    Divider(color = Color.LightGray, thickness = 1.dp)
    Spacer(Modifier.height(12.dp))
    Row {
        MenuItem(
            onClick = {
                coroutineScope.download(di)
            },
            title = "Export CSV",
            testTag = "ExportCsv",
            icon = {
                Icon(
                    painterResource("/icons/file_csv.svg"),
                    contentDescription = "Export CSV"
                )
            }
        )

    }
    Spacer(Modifier.height(12.dp))
    Row {
        MenuItem(
            onClick = {
                coroutineScope.launch {
                    upload(di)
                }
            },
            title = "Import CSV",
            testTag = "ImportCsv",
            icon = {
                Icon(
                    painterResource("/icons/upload.svg"),
                    contentDescription = "Import CSV"
                )
            }
        )
    }
    Spacer(Modifier.height(12.dp))
    Divider(color = Color.LightGray, thickness = 1.dp)
    Spacer(Modifier.height(12.dp))
    Row {
        MenuItem(
            onClick = {
                navController.navigate(Screen.Users)
            },
            title = "Users",
            testTag = "Users",
            icon = {
                Icon(
                    painterResource("/icons/file_csv.svg"),
                    contentDescription = "Users"
                )
            }
        )
    }
    Spacer(Modifier.height(12.dp))
    if (userVM.loggedUser.value.isAdmin()) {
        Row {
            MenuItem(
                onClick = {
                    navController.navigate(Screen.CreateUser)
                },
                title = "Create User",
                testTag = "CreateUser",
                icon = {
                    Icon(
                        Icons.Default.AccountBox,
                        "CreateUsers"
                    )
                }
            )
        }
        Spacer(Modifier.height(12.dp))
        Divider(color = Color.LightGray, thickness = 1.dp)
        Spacer(Modifier.height(12.dp))
        Row {
            MenuItem(
                onClick = {
                    navController.navigate(Screen.ConfigureApp)
                },
                title = "Configure",
                testTag = "Configure App",
                icon = {
                    Icon(
                        Icons.Default.Settings,
                        "Configure App"
                    )
                }
            )
        }
        Spacer(Modifier.height(12.dp))
    }
    Divider(color = Color.LightGray, thickness = 1.dp)
    Spacer(Modifier.height(12.dp))
    Row {
        MenuItem(
            onClick = {
                exitProcess(0)
            },
            title = "Exit",
            testTag = "Exit",
            icon = {
                Icon(
                    Icons.Default.ExitToApp,
                    "Exit"
                )
            }
        )
    }
}


private fun configureLog(): KLogger {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
    return KotlinLogging.logger {}
}

fun main() {
    val configFile = configureEnvironment()
    val di = di(configFile)
    val datasource by di.instance<DataSource>()
    Migration(datasource).migrate()

    application(exitProcessOnExit = true) {
        val coroutineScope = rememberCoroutineScope()
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                (datasource as HikariDataSource).close()
                coroutineScope.cancel("Shutdown")
            }
        })
        Window(
            icon = painterResource("/icons/lockoverlay.png"),
            onCloseRequest = {
                exitApplication()
            },
            title = "Password Store - ${getMode()}",
            state = rememberWindowState(width = 1024.dp, height = 768.dp)
        ) {
            LOGGER.info { "Building the app" }
            app(di)
        }
    }
}

val LOGGER = configureLog()

