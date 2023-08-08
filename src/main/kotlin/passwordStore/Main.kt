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
import kotlinx.coroutines.CoroutineScope
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
import passwordStore.config.SetupEnv
import passwordStore.navigation.NavController
import passwordStore.navigation.rememberNavController
import passwordStore.services.exportPath
import passwordStore.services.performDownload
import passwordStore.sql.Migration
import passwordStore.users.UserVM
import passwordStore.utils.Platform
import passwordStore.utils.StatusHolder
import java.nio.file.Files
import java.nio.file.Path
import javax.sql.DataSource
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists
import kotlin.io.path.writer

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
            StatusHolder.scaffoldState.drawerState.close()
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
                                        StatusHolder.scaffoldState.drawerState.open()
                                    } else {
                                        StatusHolder.scaffoldState.drawerState.close()
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
                    Text("Password Store")
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
fun drawer(navController: NavController) {
    val di: DI = localDI()
    val coroutineScope = rememberCoroutineScope()
    val userVM by rememberInstance<UserVM>()

    Row {

        IconButton(
            onClick = { navController.navigate(Screen.List) },
            modifier = Modifier.testTag("Home").align(Alignment.CenterVertically).size(24.dp)
        ) {
            Icon(
                Icons.Default.Home, contentDescription = Screen.List.name,
            )
        }
        Text(
            text = Screen.List.name,
            style = MaterialTheme.typography.body1,
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
            modifier = Modifier.testTag("New Service").align(Alignment.CenterVertically).size(24.dp)
        ) {
            Icon(
                Icons.Default.Create, contentDescription = Screen.NewService.name,

                )
        }
        Text(
            text = Screen.NewService.name,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.clickable {
                navController.navigate(Screen.NewService)
            }.align(Alignment.CenterVertically)
        )
    }
    Spacer(Modifier.height(12.dp))
    Row {
        IconButton(
            onClick = { navController.navigate(Screen.History) },
            modifier = Modifier.align(Alignment.CenterVertically).size(24.dp)
        ) {

            Icon(
                painterResource("/icons/history.svg"),
                contentDescription = Screen.History.name
            )
        }
        Text(
            text = Screen.History.name,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.clickable {
                navController.navigate(Screen.History)
            }.align(Alignment.CenterVertically)
        )
    }
    Spacer(Modifier.height(12.dp))
    Divider(color = Color.LightGray, thickness = 1.dp)
    Spacer(Modifier.height(12.dp))
    Row {
        IconButton(
            onClick = {
                coroutineScope.download(di)
            },
            modifier = Modifier.align(Alignment.CenterVertically).size(24.dp)
        ) {

            Icon(
                painterResource("/icons/file_csv.svg"),
                contentDescription = "Export CSV"
            )
        }
        Text(
            text = "Export CSV",
            style = MaterialTheme.typography.body1,
            modifier = Modifier.clickable {
                coroutineScope.download(di)
            }.align(Alignment.CenterVertically)
        )
    }
    Spacer(Modifier.height(12.dp))
    Divider(color = Color.LightGray, thickness = 1.dp)
    Spacer(Modifier.height(12.dp))
    Row {
        IconButton(
            onClick = {
                navController.navigate(Screen.Users)
            },
            modifier = Modifier.align(Alignment.CenterVertically).size(24.dp)
        ) {
            Icon(
                Icons.Default.AccountCircle,
                "Users"
            )
        }
        Text(
            text = "Users",
            style = MaterialTheme.typography.body1,
            modifier = Modifier.clickable {
                navController.navigate(Screen.Users)
            }.align(Alignment.CenterVertically)
        )
    }
    Spacer(Modifier.height(12.dp))
    if (userVM.loggedUser.value.isAdmin()) {
        Row {
            IconButton(
                onClick = {
                    navController.navigate(Screen.Users)
                },
                modifier = Modifier.align(Alignment.CenterVertically).size(24.dp)
            ) {
                Icon(
                    Icons.Default.AccountBox,
                    "Users"
                )
            }
            Text(
                text = "Create User",
                style = MaterialTheme.typography.body1,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.CreateUser)
                }.align(Alignment.CenterVertically)
            )
        }
    }
}

private fun CoroutineScope.download(di: DI) {
    val exportPath = exportPath()
    LOGGER.warn { "Writing in directory: $exportPath" }

    launch(Dispatchers.IO) {
        StatusHolder.scaffoldState.drawerState.close()
        exportPath.writer().use {
            it.performDownload(di)
        }
        StatusHolder.sendMessage("Download completed")
    }
}


private fun configureLog(): KLogger {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
    return KotlinLogging.logger {}
}

fun main() {
    configureEnvironment()
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

            app(di)
        }
    }
}

fun configureEnvironment() {
    val mode = System.getProperty("kpassword-store.mode", "TEST")

    val configFile = if (mode == "PROD") {
        val dir = configDir()
        dir.createDirectories()
        dir.resolve("db.properties").also { path ->
            if (path.notExists()) {
                val template =
                    Thread.currentThread().contextClassLoader.getResource("/config.properties.template")?.readText()
                        .orEmpty()
                Files.writeString(path, template)
            }
        }
    } else {
        Path.of(".env")
    }
    LOGGER.info { "Reading configuration for file ${configFile.toAbsolutePath()}" }
    SetupEnv.configure(configFile)
}

fun configDir(): Path {
    val os = Platform.os()
    val userHome = System.getProperty("user.home")
    return os.configDirectory(userHome)
}

val LOGGER = configureLog()