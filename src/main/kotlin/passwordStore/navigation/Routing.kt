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
import passwordStore.services.*
import passwordStore.users.UserVM
import passwordStore.users.createUser
import passwordStore.users.userSettings
import passwordStore.users.users
import passwordStore.utils.LocalStatusHolder


@Composable
private fun withAuthentication(content: @Composable () -> Unit) {
    val userVM by rememberInstance<UserVM>()
    check(userVM.loggedUser.value != UserVM.NONE) {
        "Access denied"
    }
    content()
}

@Composable
private fun withCloseDrawer(content: @Composable () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val statusHolder = LocalStatusHolder.currentOrThrow
    coroutineScope.launch(Dispatchers.Main) {
        statusHolder.closeDrawer()
    }
    content()
}

sealed interface KPasswordScreen {
    val name: String
    val allowBack: Boolean
        get() = false

    data object Home : Screen, KPasswordScreen {
        private fun readResolve(): Any = Home
        override val name: String
            get() = "Home"

        @Composable
        override fun Content() = withCloseDrawer {
            withAuthentication {
                val coroutineScope = rememberCoroutineScope()
                val serviceModel by rememberInstance<ServiceVM>()
                serviceModel.resetSearch()
                coroutineScope.launch(Dispatchers.Main) {
                    serviceModel.fetchAll()
                }
                servicesTable()
            }
        }
    }


    data object Login : Screen, KPasswordScreen {
        private fun readResolve(): Any = Login
        override val name: String
            get() = "Login"

        @Composable
        override fun Content() = withCloseDrawer {
            val navController = LocalNavigator.currentOrThrow
            val serviceModel by rememberInstance<ServiceVM>()
            val userVM by rememberInstance<UserVM>()
            loginPane(loginFunction = { currentUsername, pwd ->
                userVM.login(currentUsername, pwd).onSuccess {
                    userVM.loggedUser.value = it
                    navController.push(Home)
                }
            })

        }
    }


    object NewService : Screen, KPasswordScreen {
        private fun readResolve(): Any = NewService
        override val allowBack: Boolean
            get() = true
        override val name: String
            get() = "New Service"

        @Composable
        override fun Content() = withCloseDrawer {
            withAuthentication {
                val navController = LocalNavigator.currentOrThrow
                val serviceModel by rememberInstance<ServiceVM>()
                val userVM by rememberInstance<UserVM>()
                val coroutineScope = rememberCoroutineScope()
                newService(Service(), onCancel = { navController.pop() }) {
                    coroutineScope.launch(Dispatchers.IO) {
                        serviceModel.store(it).onSuccess {
                            withContext(Dispatchers.Main) {
                                navController.push(Home)
                            }
                        }

                    }
                }
            }
        }
    }

    object History : Screen, KPasswordScreen {
        private fun readResolve(): Any = History
        override val name: String
            get() = "History"
        override val allowBack: Boolean
            get() = true

        @Composable
        override fun Content() = withCloseDrawer {
            withAuthentication {
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
    }

    object UserSettings : Screen, KPasswordScreen {
        private fun readResolve(): Any = UserSettings
        override val name: String
            get() = "Settings"

        override val allowBack: Boolean
            get() = true

        @Composable
        override fun Content() = withCloseDrawer {
            withAuthentication {
                val userVM by rememberInstance<UserVM>()
                userSettings(userVM.loggedUser.value)
            }
        }
    }

    object Users : Screen, KPasswordScreen {
        private fun readResolve(): Any = Users
        override val name: String
            get() = "Users"

        @Composable
        override fun Content() = withCloseDrawer {
            withAuthentication {
                val userVM by rememberInstance<UserVM>()
                val coroutineScope = rememberCoroutineScope()
                coroutineScope.launch {
                    userVM.loadUsers()
                }
                users()
            }
        }
    }

    object CreateUser : Screen, KPasswordScreen {
        private fun readResolve(): Any = CreateUser
        override val name: String
            get() = "Create User"

        override val allowBack: Boolean
            get() = true

        @Composable
        override fun Content() = withCloseDrawer {
            withAuthentication {
                createUser()
            }
        }
    }

    object ConfigureApp : Screen, KPasswordScreen {
        private fun readResolve(): Any = ConfigureApp
        override val name: String
            get() = "Configure"

        override val allowBack: Boolean
            get() = true

        @Composable
        override fun Content() = withCloseDrawer {
            withAuthentication {
                configView()
            }
        }
    }
}