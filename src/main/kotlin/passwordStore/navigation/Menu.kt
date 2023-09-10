package passwordStore.navigation

import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.compose.localDI
import org.kodein.di.compose.rememberInstance
import passwordStore.services.download
import passwordStore.services.upload
import passwordStore.users.UserVM
import passwordStore.widget.menuItem
import kotlin.system.exitProcess


@Composable
fun menu() {
    val navController = LocalNavigator.currentOrThrow
    val di: DI = localDI()
    val coroutineScope = rememberCoroutineScope()
    val userVM by rememberInstance<UserVM>()

    menuItem(
        onClick = { navController.push(KPasswordScreen.List) },
        title = KPasswordScreen.List.name,
        testTag = "Home",
        icon = {
            Icon(
                Icons.Default.Home, contentDescription = KPasswordScreen.List.name,
            )
        }
    )
    menuItem(
        onClick = { navController.push(KPasswordScreen.NewService) },
        title = KPasswordScreen.NewService.name,
        testTag = "New Service",
        icon = {
            Icon(
                Icons.Default.Create, contentDescription = KPasswordScreen.NewService.name,
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

    menuItem(
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
    if (userVM.isAdmin()) {
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
    if (userVM.loggedUser.value.id > 0) {
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
    menuItem(
        onClick = {
            navController.popUntilRoot()
            userVM.loggedUser.value = UserVM.NONE
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
                Icons.Default.PowerOff,
                "Exit"
            )
        }
    )

}