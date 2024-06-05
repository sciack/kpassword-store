package passwordStore.navigation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import passwordStore.LOGGER
import passwordStore.users.LocalSetUser
import passwordStore.users.LocalUser
import passwordStore.users.admin
import passwordStore.users.isAuthenticated
import passwordStore.utils.LocalStatusHolder
import passwordStore.widget.menuItem
import passwordStore.widget.showAbout
import kotlin.system.exitProcess


@Composable
fun ColumnScope.menu() {
    val navController = LocalNavigator.currentOrThrow
    val statusHolder = LocalStatusHolder.currentOrThrow
    val coroutineScope = rememberCoroutineScope()

    val user = LocalUser.current
    val setUser = LocalSetUser.current

    LOGGER.debug { "Current user: $user" }

    menuItem(
        onClick = { navController.push(KPasswordScreen.Home) },
        title = KPasswordScreen.Home.name,
        testTag = "Home",
        selected = navController.lastItem is KPasswordScreen.Home,
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
        selected = navController.lastItem is KPasswordScreen.NewService,
        icon = {
            Icon(
                Icons.Default.Add, contentDescription = KPasswordScreen.NewService.name,
            )
        }
    )
    val serviceHistory = remember {
        KPasswordScreen.ServiceHistory(null)
    }
    menuItem(
        onClick = { navController.push(serviceHistory) },
        title = serviceHistory.name,
        testTag = "History",
        selected = navController.lastItem is KPasswordScreen.ServiceHistory,
        icon = {
            Icon(
                painterResource("/icons/history.svg"),
                contentDescription = serviceHistory.name
            )
        }
    )

    HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
    menuItem(
        onClick = {
            coroutineScope.launch {
                navController.push(KPasswordScreen.ExportCsv)
                statusHolder.closeDrawer()
            }
        },
        title = "Export CSV",
        testTag = "ExportCsv",
        selected = navController.lastItem is KPasswordScreen.ExportCsv,
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
                navController.push(KPasswordScreen.LoadCsv)
                statusHolder.closeDrawer()
            }
        },
        title = "Import CSV",
        testTag = "ImportCsv",
        selected = navController.lastItem is KPasswordScreen.LoadCsv,
        icon = {
            Icon(
                painterResource("/icons/upload.svg"),
                contentDescription = "Import CSV"
            )
        }
    )
    HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
    menuItem(
        onClick = {
            navController.push(KPasswordScreen.Users)
        },
        title = "Users",
        testTag = "Users",
        selected = navController.lastItem is KPasswordScreen.Users,
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
            selected = navController.lastItem is KPasswordScreen.CreateUser,
            icon = {
                Icon(
                    Icons.Default.PersonAdd,
                    "CreateUsers"
                )
            }
        )

    }
    HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
    menuItem(
        onClick = {
            navController.push(KPasswordScreen.ConfigureApp)
        },
        title = "Configure",
        testTag = "Configure App",
        selected = navController.lastItem is KPasswordScreen.ConfigureApp,
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
            selected = navController.lastItem is KPasswordScreen.UserSettings,
            icon = {
                Icon(Icons.Default.Person, "Settings")
            }
        )
    }
    HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
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
        selected = false,
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
                Icons.AutoMirrored.Filled.Logout,
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
                Icons.AutoMirrored.Filled.ExitToApp,
                "Exit"
            )
        }
    )
    showAbout(showAbout)
}
