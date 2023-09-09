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
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.compose.localDI
import org.kodein.di.compose.rememberInstance
import passwordStore.Screen
import passwordStore.services.download
import passwordStore.services.upload
import passwordStore.users.UserVM
import passwordStore.widget.menuItem
import kotlin.system.exitProcess


@Composable
fun menu(navController: NavController) {
    val di: DI = localDI()
    val coroutineScope = rememberCoroutineScope()
    val userVM by rememberInstance<UserVM>()

    menuItem(
        onClick = { navController.navigate(Screen.List) },
        title = Screen.List.name,
        testTag = "Home",
        icon = {
            Icon(
                Icons.Default.Home, contentDescription = Screen.List.name,
            )
        }
    )
    menuItem(
        onClick = { navController.navigate(Screen.NewService) },
        title = Screen.NewService.name,
        testTag = "New Service",
        icon = {
            Icon(
                Icons.Default.Create, contentDescription = Screen.NewService.name,
            )
        }
    )

    menuItem(
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
    if (userVM.loggedUser.value.isAdmin()) {
        menuItem(
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
        Divider(color = Color.LightGray, thickness = 1.dp)
        menuItem(
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
    Divider(color = Color.LightGray, thickness = 1.dp)
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

}