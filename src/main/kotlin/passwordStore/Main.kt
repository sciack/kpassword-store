package passwordStore

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import passwordStore.config.ConfigVM
import passwordStore.config.MODE
import passwordStore.config.getMode
import passwordStore.navigation.KPasswordScreen
import passwordStore.navigation.menu
import passwordStore.ui.theme.SMALL
import passwordStore.ui.theme.XXL
import passwordStore.ui.theme.appTheme
import passwordStore.users.UserVM
import passwordStore.utils.LocalStatusHolder
import passwordStore.utils.StatusHolder
import passwordStore.utils.configureLog
import passwordStore.widget.APP_BAR_HEIGHT
import passwordStore.widget.AppWindowTitleBar

@Composable
@Preview
fun app() {
    val userVM by rememberInstance<UserVM>()
    val statusHolder = LocalStatusHolder.currentOrThrow
    Scaffold(Modifier.then(Modifier.fillMaxSize()), scaffoldState = statusHolder.scaffoldState, topBar = {
        TopAppBar(
            modifier = Modifier.height(APP_BAR_HEIGHT)
        ) {}
    }) {

        ModalDrawer(
            drawerState = statusHolder.scaffoldState.drawerState,
            drawerContent = {
                menu()
            },
            drawerShape = customShape(),
        ) {

            CurrentScreen()
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
        val state = rememberWindowState(size = DpSize(1600.dp, 900.dp))
        Window(
            icon = painterResource("/icons/lockoverlay.png"), onCloseRequest = {
                exitApplication()
            }, undecorated = true,

            state = state
        ) {
            withDI(di) {
                val configVM by rememberInstance<ConfigVM>()
                val darkMode = remember { configVM.darkMode }

                appTheme(
                    darkMode.value.isDarkMode()
                ) {
                    LOGGER.info { "Building the app" }
                    Navigator(KPasswordScreen.Login) {
                        val scaffoldState = rememberScaffoldState()
                        CompositionLocalProvider(
                            LocalStatusHolder provides StatusHolder(scaffoldState)
                        ) {
                            app()
                            val title = remember {
                                mutableStateOf(prefix)
                            }
                            val userVM by rememberInstance<UserVM>()
                            val user = remember {
                                userVM.loggedUser
                            }
                            AppWindowTitleBar(title = {
                                Text(
                                    title.value,
                                    color = MaterialTheme.colors.onPrimary,
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    fontWeight = FontWeight.Bold
                                )
                                if(user.value != UserVM.NONE) {
                                    Spacer(Modifier.width(SMALL))
                                    Text(
                                        user.value.fullName,
                                        color = MaterialTheme.colors.onPrimary,
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                                state = state,
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
    }
}

@Composable
fun menuDrawer() {
    val navController = LocalNavigator.currentOrThrow
    val userVM by rememberInstance<UserVM>()
    val currentUser = remember {
        userVM.loggedUser
    }
    val coroutineScope = rememberCoroutineScope()
    Row {
        val currentScreen = navController.lastItem as KPasswordScreen
        Image(painterResource("/icons/lockoverlay.png"), "App Icon", modifier = Modifier.size(XXL))
        if (currentScreen.allowBack) {
            IconButton(
                onClick = { navController.pop() }, modifier = Modifier.padding(start = SMALL)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = MaterialTheme.colors.onPrimary,
                    modifier = Modifier.size(XXL)
                )
            }
        }
        if (currentUser.value != UserVM.NONE) {
            val statusHolder = LocalStatusHolder.currentOrThrow
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        if (statusHolder.scaffoldState.drawerState.isClosed) {
                            statusHolder.openDrawer()
                        } else {
                            statusHolder.closeDrawer()
                        }
                    }
                }, modifier = Modifier.testTag("Drawer").padding(start = SMALL).size(XXL)
            ) {
                Icon(
                    Icons.Default.Menu, contentDescription = "", tint = MaterialTheme.colors.onPrimary,
                    modifier = Modifier.size(XXL)
                )
            }
        }
    }
}

val LOGGER = configureLog()


