package passwordStore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.compose.localDI
import org.kodein.di.compose.rememberInstance
import passwordStore.navigation.NavController
import passwordStore.navigation.NavigationHost
import passwordStore.navigation.authenticatedComposable
import passwordStore.navigation.composable
import passwordStore.services.*
import passwordStore.users.User
import passwordStore.users.userSettings

sealed interface Screen {
    val name: String
    val allowBack: Boolean
        get() = false

    object List : Screen {
        override val name: String
            get() = "List"
    }

    object Login : Screen {
        override val name: String
            get() = "Login"
    }

    data class Details(val service: Service) : Screen {
        override val name: String
            get() = "Details"

        override val allowBack: Boolean
            get() = true
    }

    object NewService : Screen {
        override val allowBack: Boolean
            get() = true
        override val name: String
            get() = "New Service"
    }

    object History : Screen {
        override val name: String
            get() = "History"
        override val allowBack: Boolean
            get() = true
    }

    object Settings : Screen {
        override val name: String
            get() = "Settings"

        override val allowBack: Boolean
            get() = true
    }
}

@Composable
fun route(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val serviceModel by rememberInstance<ServiceViewModel>()
    val di = localDI()
    val user = remember {
        mutableStateOf<User?>(null)
    }

    NavigationHost(navController) {
        composable(Screen.Login) {

            loginPane(loginFunction = { currentUsername, pwd ->
                submit(di, currentUsername, pwd).onSuccess {
                    user.value = it
                    serviceModel.user.value = it
                    navController.navigate(Screen.List)
                }
            })
        }

        authenticatedComposable(Screen.List) {

            serviceModel.resetService()
            serviceModel.fetchAll()
            servicesTable()
        }

        authenticatedComposable(Screen.NewService) {

            newService {
                coroutineScope.launch(Dispatchers.IO) {
                    serviceModel.store(it).onSuccess {
                        serviceModel.resetService()
                        withContext(Dispatchers.Main) {
                            navController.navigate(Screen.List)
                        }
                    }

                }
            }
        }

        authenticatedComposable(Screen.History) {

            historyTable(serviceModel.historyEvents.value)
            if (serviceModel.shouldLoadHistory()) {
                coroutineScope.launch(Dispatchers.IO) {
                    serviceModel.history("", exactMatch = false)
                }
            }
        }

        authenticatedComposable(Screen.Settings) {
            userSettings(serviceModel.user.value)
        }

    }.build()
}