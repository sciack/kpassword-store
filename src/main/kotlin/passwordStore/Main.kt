package passwordStore

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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

    Scaffold(
        Modifier.then(Modifier.fillMaxSize()),
        scaffoldState = StatusHolder.scaffoldState,
        topBar = {
            topAppBar()
        }
    ) {

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


@Composable
fun topAppBar() {
    val navController by rememberNavController()
    val userVM by rememberInstance<UserVM>()
    val currentUser = remember {
        userVM.loggedUser
    }

    val coroutineScope = rememberCoroutineScope()
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
                modifier = Modifier.testTag("Drawer").padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Menu, contentDescription = "")
            }
        }
        if (navController.currentScreen.value.allowBack) {
            IconButton(
                onClick = { navController.navigateBack() },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Back"
                )
            }
        }
    }, title = {
        Spacer(modifier = Modifier.width(24.dp))
        Text("Password Store")
        if (getMode() == MODE.TEST) {
            Text(" - ${getMode()}")
        }
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
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(Icons.Default.Settings, "Settings")
            }
    })
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
    application(exitProcessOnExit = true) {
        val state = rememberWindowState(size = DpSize.Unspecified)
        Window(
            icon = painterResource("/icons/lockoverlay.png"),
            onCloseRequest = {
                exitApplication()
            },
            undecorated = false,
            title = buildString {
                append("Password Store")
                if (getMode() == MODE.TEST) {
                    append(" - ")
                    append(getMode())
                }
            },
            state = state
        ) {
            appTheme {
                LOGGER.info { "Building the app" }
                withDI(di) {
                    app()
                }
            }
        }
    }
}

val LOGGER = configureLog()


