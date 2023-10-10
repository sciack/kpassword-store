package passwordStore.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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


        @Composable
        override fun Content() = withCloseDrawer {
            withAuthentication {
                val coroutineScope = rememberCoroutineScope()
                val serviceSM = rememberScreenModel<ServicesSM>()
                val user = LocalUser.currentOrThrow
                serviceSM.resetSearch()
                servicesTable(serviceSM)
                coroutineScope.launch(Dispatchers.Main) {
                    serviceSM.fetchAll(user)
                }
            }
        }
    }

    data object ExportCsv : Screen, KPasswordScreen {
        private fun readResolve(): Any = ExportCsv

        override val name: String
            get() = "Export Csv"

        override val allowBack: Boolean
            get() = false

        @Composable
        override fun Content() {
            download(rememberScreenModel())
        }
    }

    data object LoadCsv : Screen, KPasswordScreen {
        private fun readResolve(): Any = LoadCsv

        override val name: String
            get() = "Load Csv"

        override val allowBack: Boolean
            get() = false

        @Composable
        override fun Content() {
            upload(rememberScreenModel())
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
                val createServiceSM = rememberScreenModel<CreateServiceSM>()
                newService(createServiceSM)
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
                history(rememberScreenModel<HistorySM>(), service)
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
                Box(
                    Modifier.fillMaxSize()
                ) {
                    ElevatedCard(
                        Modifier.align(Alignment.Center)
                            .fillMaxHeight(0.8f)
                    ) {
                        userSettings(rememberScreenModel())
                    }
                }
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