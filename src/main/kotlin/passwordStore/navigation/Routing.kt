package passwordStore.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.kodein.di.compose.rememberInstance
import passwordStore.config.configView
import passwordStore.loginPane
import passwordStore.services.*
import passwordStore.users.*
import passwordStore.utils.LocalStatusHolder


@Composable
private fun withAuthentication(content: @Composable () -> Unit) {
    val user = LocalUser.current
    check(user.isAuthenticated()) {
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

        private val reload = MutableStateFlow(false)
        @Composable
        override fun Content() = withCloseDrawer {
            withAuthentication {
                val coroutineScope = rememberCoroutineScope()
                val serviceSM = rememberScreenModel<ServicesSM>()
                val user = LocalUser.currentOrThrow
                val shouldReload by reload.collectAsState()
                if (shouldReload) {
                    coroutineScope.launch(Dispatchers.Main) {
                        reload.emit(false)
                    }
                }
                serviceSM.resetSearch()
                servicesTable(serviceSM)
                coroutineScope.launch(Dispatchers.Main) {
                    serviceSM.fetchAll(user)
                }
            }
        }

        fun reload() {
            runBlocking {
                launch {
                    reload.emit(true)
                }
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
            val serviceModel by rememberInstance<ServicesSM>()
            val loginSM = rememberScreenModel<LoginSM>()
            val setUser = LocalSetUser.current
            loginPane(loginFunction = { currentUsername, pwd ->
                loginSM.login(currentUsername, pwd).onSuccess {
                    setUser(it)
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
                val createServiceSM = rememberScreenModel<CreateServiceSM>()
                val coroutineScope = rememberCoroutineScope()
                newService(createServiceSM.saveError, onCancel = { navController.pop() }) {
                    coroutineScope.launch(Dispatchers.IO) {
                        createServiceSM.store(it).onSuccess {
                            withContext(Dispatchers.Main) {
                                navController.push(Home)
                            }
                        }

                    }
                }
            }
        }
    }

    data class ServiceHistory(val service: Service?) : KPasswordScreen, Screen {
        override val name: String
            get() = "History"

        override val allowBack: Boolean
            get() = true

        @Composable
        override fun Content() = withCloseDrawer {
            withAuthentication {
                val coroutineScope = rememberCoroutineScope()
                val historySM = rememberScreenModel<HistorySM>()
                val user = LocalUser.currentOrThrow
                historyTable(historySM)
                coroutineScope.launch(Dispatchers.IO) {
                    if(service == null) {
                        historySM.history("", exactMatch = false, user)
                    } else {
                        historySM.history(service.service, exactMatch = true, user)
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
                userSettings(rememberScreenModel())
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
                val userVM = rememberScreenModel<UserVM>()
                val coroutineScope = rememberCoroutineScope()
                coroutineScope.launch {
                    userVM.loadUsers()
                }
                users(userVM)
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
                createUser(createUserSM = rememberScreenModel())
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
                configView(rememberScreenModel())
            }
        }
    }
}