package passwordStore

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import passwordStore.config.MODE
import passwordStore.config.getMode
import passwordStore.navigation.menu
import passwordStore.navigation.rememberNavController
import passwordStore.users.UserVM
import passwordStore.utils.StatusHolder
import passwordStore.utils.configureLog
import passwordStore.widget.AppWindowTitleBar
import ui.theme.appTheme

@Composable
@Preview
fun app() {
    val navController by rememberNavController()
    val userVM by rememberInstance<UserVM>()
    val coroutineScope = rememberCoroutineScope()
    StatusHolder.scaffoldState = rememberScaffoldState()

    navController.onSelection {
        coroutineScope.launch(Dispatchers.Main) {
            StatusHolder.closeDrawer()
        }
    }

    Scaffold(Modifier.then(Modifier.fillMaxSize()), scaffoldState = StatusHolder.scaffoldState, topBar = {
        TopAppBar(
            modifier = Modifier.height(24.dp)
        ) {}
    }) {

        ModalDrawer(
            drawerState = StatusHolder.scaffoldState.drawerState,
            drawerContent = {
                menu(navController)
            },
            drawerShape = customShape(),
        ) {
            route(navController)
        }
    }
}

fun customShape() = object : Shape {
    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
    ): Outline {
        return Outline.Rectangle(Rect(0f, 0f, 400f, size.height))
    }
}


fun main() {
    LOGGER.warn {
        """
            Starting KPassword Store - ${getMode()}
            Using JVM: ${System.getProperty("java.version")} - ${System.getProperty("java.vendor")}
        """.trimIndent()
    }
    val di = di()
    val datasource by di.instance<HikariDataSource>()
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            datasource.close()
        }
    })
    val prefix = buildString {
        append("Password Store")
        if (getMode() == MODE.TEST) {
            append(" - ")
            append(getMode())
        }
    }

    application(exitProcessOnExit = true) {
        val state = rememberWindowState(size = DpSize.Unspecified)
        Window(
            icon = painterResource("/icons/lockoverlay.png"), onCloseRequest = {
                exitApplication()
            }, undecorated = true,

            state = state
        ) {
            appTheme {
                LOGGER.info { "Building the app" }
                withDI(di) {
                    app()
                    val title = remember {
                        mutableStateOf(prefix)
                    }
                    AppWindowTitleBar(title = title,
                        onMinimize = { state.isMinimized = state.isMinimized.not() },
                        onMaximize = {
                            state.placement =
                                if (state.placement == WindowPlacement.Maximized) WindowPlacement.Floating else WindowPlacement.Maximized
                        },
                        onClose = {
                            exitApplication()
                        }) {
                        menuDrawer()
                    }
                }
            }
        }
    }
}

@Composable
fun menuDrawer() {
    val navController by rememberNavController()
    val userVM by rememberInstance<UserVM>()
    val currentUser = remember {
        userVM.loggedUser
    }
    val coroutineScope = rememberCoroutineScope()
    Row {
        Image(painterResource("/icons/lockoverlay.png"), "App Icon", modifier = Modifier.size(24.dp))
        if (navController.currentScreen.value.allowBack) {
            IconButton(
                onClick = { navController.navigateBack() }, modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = MaterialTheme.colors.onPrimary
                )
            }
        }
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
                }, modifier = Modifier.testTag("Drawer").padding(start = 8.dp)
            ) {
                Icon(
                    Icons.Default.Menu, contentDescription = "", tint = MaterialTheme.colors.onPrimary
                )
            }
        }
    }
}

val LOGGER = configureLog()


