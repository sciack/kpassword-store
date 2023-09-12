package passwordStore.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.compose.localDI
import passwordStore.LOGGER
import passwordStore.config.LocalVersion
import passwordStore.services.download
import passwordStore.services.upload
import passwordStore.ui.theme.LARGE
import passwordStore.users.LocalSetUser
import passwordStore.users.LocalUser
import passwordStore.users.admin
import passwordStore.users.isAuthenticated
import passwordStore.utils.LocalStatusHolder
import passwordStore.widget.menuItem
import passwordStore.widget.showAbout
import kotlin.system.exitProcess


@Composable
fun menu() {
    val navController = LocalNavigator.currentOrThrow
    val statusHolder = LocalStatusHolder.currentOrThrow
    val di: DI = localDI()
    val coroutineScope = rememberCoroutineScope()

    val user = LocalUser.current
    val setUser = LocalSetUser.current

    LOGGER.warn { "Current user: $user" }
    menuItem(
        onClick = { navController.push(KPasswordScreen.Home) },
        title = KPasswordScreen.Home.name,
        testTag = "Home",
        icon = {
            Icon(
                Icons.Default.Home, contentDescription = KPasswordScreen.Home.name,
            )
        }
    )
    menuItem(
        onClick = { navController.push(KPasswordScreen.NewService) },
        title = KPasswordScreen.NewService.name,
        testTag = "New Service",
        icon = {
            Icon(
                Icons.Default.Add, contentDescription = KPasswordScreen.NewService.name,
            )
        }
    )

    menuItem(
        onClick = { navController.push(KPasswordScreen.History) },
        title = KPasswordScreen.History.name,
        testTag = "History",
        icon = {
            Icon(
                painterResource("/icons/history.svg"),
                contentDescription = KPasswordScreen.History.name
            )
        }
    )

    Divider(color = Color.LightGray, thickness = 1.dp)
    menuItem(
        onClick = {
            coroutineScope.download(di, statusHolder, user!!)
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

    menuItem(
        onClick = {
            coroutineScope.launch {
                upload(di, statusHolder, user!!)
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
    Divider(color = Color.LightGray, thickness = 1.dp)
    menuItem(
        onClick = {
            navController.push(KPasswordScreen.Users)
        },
        title = "Users",
        testTag = "Users",
        icon = {
            Icon(
                Icons.Default.People,
                contentDescription = "Users"
            )
        }
    )
    if (user.admin()) {
        menuItem(
            onClick = {
                navController.push(KPasswordScreen.CreateUser)
            },
            title = "Create User",
            testTag = "CreateUser",
            icon = {
                Icon(
                    Icons.Default.PersonAdd,
                    "CreateUsers"
                )
            }
        )

    }
    Divider(color = Color.LightGray, thickness = 1.dp)
    menuItem(
        onClick = {
            navController.push(KPasswordScreen.ConfigureApp)
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
    if (user.isAuthenticated()) {
        menuItem(
            onClick = {
                navController.push(KPasswordScreen.UserSettings)
            },
            title = "User Settings",
            testTag = "User Settings App",
            icon = {
                Icon(Icons.Default.Person, "Settings")
            }
        )
    }
    Divider(color = Color.LightGray, thickness = 1.dp)
    val showAbout = remember { mutableStateOf(false) }
    menuItem(
        onClick = {
            showAbout.value = true
            coroutineScope.launch {
                statusHolder.closeDrawer()
            }
        },
        title = "About",
        testTag = "About",
        icon = {
            Icon(Icons.Default.QuestionMark, "About")
        }
    )
    menuItem(
        onClick = {
            navController.popUntilRoot()
            setUser(null)
        },
        title = "Logout",
        testTag = "Logout",
        icon = {
            Icon(
                Icons.Default.Logout,
                "Logout"
            )
        }
    )
    menuItem(
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
    showAbout(showAbout)
}
