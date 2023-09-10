package passwordStore.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.compose.rememberInstance
import passwordStore.config.configView
import passwordStore.loginPane
import passwordStore.services.ServiceVM
import passwordStore.services.historyTable
import passwordStore.services.newService
import passwordStore.services.servicesTable
import passwordStore.users.UserVM
import passwordStore.users.createUser
import passwordStore.users.userSettings
import passwordStore.users.users
import passwordStore.utils.StatusHolder


@Composable
private fun authenticatedCall(content: @Composable () -> Unit) {
    val userVM by rememberInstance<UserVM>()
    check(userVM.loggedUser.value.id > 0) {
        "Access denied"
    }
    withCloseDrawer {
        content()
    }
}

@Composable
private fun withCloseDrawer(content: @Composable () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    coroutineScope.launch(Dispatchers.Main) {
        StatusHolder.closeDrawer()
    }
    content()
}

sealed interface KPasswordScreen {
    val name: String
    val allowBack: Boolean
        get() = false

    data object List : Screen, KPasswordScreen {
        override val name: String
            get() = "Home"

        @Composable
        override fun Content() = withCloseDrawer{
            authenticatedCall {
                val coroutineScope = rememberCoroutineScope()
                val serviceModel by rememberInstance<ServiceVM>()
                coroutineScope.launch(Dispatchers.Main) {
                    serviceModel.resetService()
                    serviceModel.fetchAll()
                }
                servicesTable()
            }
        }
    }

    data object Login : Screen, KPasswordScreen {
        override val name: String
            get() = "Login"

        @Composable
        override fun Content() {
            val navController = LocalNavigator.currentOrThrow
            val serviceModel by rememberInstance<ServiceVM>()
            val userVM by rememberInstance<UserVM>()
            loginPane(loginFunction = { currentUsername, pwd ->
                userVM.login(currentUsername, pwd).onSuccess {
                    userVM.loggedUser.value = it
                    navController.push(List)
                }
            })

        }
    }


    object NewService : Screen, KPasswordScreen {
        override val allowBack: Boolean
            get() = true
        override val name: String
            get() = "New Service"

        @Composable
        override fun Content() = authenticatedCall {
            val navController = LocalNavigator.currentOrThrow
            val serviceModel by rememberInstance<ServiceVM>()
            val userVM by rememberInstance<UserVM>()
            val coroutineScope = rememberCoroutineScope()
            coroutineScope.launch(Dispatchers.Main) {
                serviceModel.resetService()
            }
            newService(onCancel = { navController.pop() }) {
                coroutineScope.launch(Dispatchers.IO) {
                    serviceModel.store(it).onSuccess {
                        serviceModel.resetService()
                        withContext(Dispatchers.Main) {
                            navController.push(List)
                        }
                    }

                }
            }
        }
    }

    object History : Screen, KPasswordScreen {
        override val name: String
            get() = "History"
        override val allowBack: Boolean
            get() = true

        @Composable
        override fun Content() = authenticatedCall {
            val coroutineScope = rememberCoroutineScope()
            val serviceModel by rememberInstance<ServiceVM>()
            historyTable(serviceModel.historyEvents.value)
            if (serviceModel.shouldLoadHistory()) {
                coroutineScope.launch(Dispatchers.IO) {
                    serviceModel.history("", exactMatch = false)
                }
            }
        }
    }

    object UserSettings : Screen, KPasswordScreen {
        override val name: String
            get() = "Settings"

        override val allowBack: Boolean
            get() = true

        @Composable
        override fun Content() = authenticatedCall {
            val userVM by rememberInstance<UserVM>()
            userSettings(userVM.loggedUser.value)
        }
    }

    object Users : Screen, KPasswordScreen {
        override val name: String
            get() = "Users"

        @Composable
        override fun Content() = authenticatedCall {
            val userVM by rememberInstance<UserVM>()
            val coroutineScope = rememberCoroutineScope()
            coroutineScope.launch {
                userVM.loadUsers()
            }
            users()
        }
    }

    object CreateUser : Screen, KPasswordScreen {
        override val name: String
            get() = "Create User"

        override val allowBack: Boolean
            get() = true

        @Composable
        override fun Content() = authenticatedCall {
            createUser()
        }
    }

    object ConfigureApp : Screen, KPasswordScreen {
        override val name: String
            get() = "Configure"

        override val allowBack: Boolean
            get() = true

        @Composable
        override fun Content() = authenticatedCall {
            configView()
        }
    }
}
