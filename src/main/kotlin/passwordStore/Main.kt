package passwordStore

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import passwordStore.config.*
import passwordStore.navigation.KPasswordScreen
import passwordStore.navigation.menu
import passwordStore.ui.theme.*
import passwordStore.users.LocalSetUser
import passwordStore.users.LocalUser
import passwordStore.users.User
import passwordStore.users.isAuthenticated
import passwordStore.utils.LocalStatusHolder
import passwordStore.utils.StatusHolder
import passwordStore.utils.configureLog
import passwordStore.widget.AppWindowTitleBar

val LOGGER = configureLog()

@Composable
fun app() {
    val statusHolder = LocalStatusHolder.currentOrThrow

    Scaffold(
        Modifier.then(
            Modifier.fillMaxSize().border(
                1.dp, color = MaterialTheme.colorScheme.primary, shape = RectangleShape
            ),
        ),
        snackbarHost = { SnackbarHost(statusHolder.snackbarHostState) },
    ) {

        ModalNavigationDrawer(
            drawerState = statusHolder.drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    menu()
                }
            },
            modifier = Modifier.fillMaxSize()
                .padding(top = it.calculateTopPadding() + XL, bottom = it.calculateBottomPadding())
        ) {
            CurrentScreen()
        }
    }
}


@Composable
fun menuDrawer() {
    val navController = LocalNavigator.currentOrThrow
    val user = LocalUser.current

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
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(XXL)
                )
            }
        }
        if (user.isAuthenticated()) {
            val statusHolder = LocalStatusHolder.currentOrThrow
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        if (statusHolder.drawerState.isClosed) {
                            statusHolder.openDrawer()
                        } else {
                            statusHolder.closeDrawer()
                        }
                    }
                }, modifier = Modifier.testTag("Drawer").padding(start = SMALL).size(XXL)
            ) {
                Icon(
                    Icons.Default.Menu, contentDescription = "", tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(XXL)
                )
            }
        }
    }
}


@Composable
private fun FrameWindowScope.appTitle(state: WindowState, onClose: () -> Unit) {

    val user = LocalUser.current
    val title = remember {
        val prefix = buildString {
            append("Password Store")
            if (getMode() == MODE.TEST) {
                append(" - ")
                append(getMode())
            }
        }
        mutableStateOf(prefix)
    }

    AppWindowTitleBar(
        title = {
            Text(
                title.value,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.align(Alignment.CenterVertically),
                fontWeight = FontWeight.Bold
            )
            if (user.isAuthenticated()) {
                Spacer(Modifier.width(SMALL))
                Text(
                    user?.fullName.orEmpty(),
                    color = MaterialTheme.colorScheme.onPrimary,
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
        onClose = onClose
    ) {
        menuDrawer()
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    val di = di()
    val datasource by di.instance<HikariDataSource>()
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            datasource.close()
        }
    })


    application(exitProcessOnExit = true) {
        val version = useResource("/version.json") {
            LOGGER.info { "Loading version file" }
            Json.decodeFromStream<Map<String, String>>(it)
        }
        val state = rememberWindowState(size = DpSize(1600.dp, 900.dp))
        Window(
            icon = painterResource("/icons/lockoverlay.png"), onCloseRequest = {
                exitApplication()
            },
            undecorated = true,
            resizable = true,
            state = state
        ) {
            val (user, setUser) = remember {
                mutableStateOf<User?>(null)
            }
            val configuredDarkMode =
                runCatching { DarkModes.valueOf(System.getProperty(DARK_MODE)) }.getOrDefault(DarkModes.SYSTEM_DEFAULT)
            val (darkMode, setDarkMode) = remember {
                mutableStateOf(configuredDarkMode)
            }
            withDI(di) {
                CompositionLocalProvider(
                    LocalDarkMode provides darkMode,
                    LocalSetDarkMode provides setDarkMode
                ) {
                    appTheme(
                        darkMode.isDarkMode()
                    ) {
                        LOGGER.info { "Building the app" }
                        Navigator(KPasswordScreen.Login) {
                            val snackbarHostState = remember { SnackbarHostState() }
                            val drawerState = remember { DrawerState(DrawerValue.Closed) }
                            CompositionLocalProvider(
                                LocalStatusHolder provides StatusHolder(snackbarHostState, drawerState),
                                LocalUser provides user,
                                LocalSetUser provides setUser,
                                LocalVersion provides version["version"]!!
                            ) {
                                app()
                                appTitle(state, onClose = ::exitApplication)
                            }
                        }
                    }
                }
            }
        }
    }
}


