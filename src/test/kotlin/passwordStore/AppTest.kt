package passwordStore


import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.SemanticsActions.RequestFocus
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import com.github.javafaker.Faker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.awaitility.kotlin.await
import org.junit.Rule
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import passwordStore.navigation.KPasswordScreen
import passwordStore.services.Service
import passwordStore.services.ServicesRepository
import passwordStore.services.ServicesSM
import passwordStore.users.UserRepository
import passwordStore.users.UserVM
import passwordStore.users.UserVM.Companion.NONE
import passwordStore.utils.LocalStatusHolder
import passwordStore.utils.StatusHolder
import passwordStore.utils.currentDateTime
import java.time.Duration
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class AppTest {
    private val di = DiInjection.testDi
    private val user = testUser()
    private val clock: Clock by di.instance()
    private val servicesRepository by di.instance<ServicesRepository>()
    private val serviceModel by di.instance<ServicesSM>()
    private val faker = Faker()
    private val userRepository by di.instance<UserRepository>()
    private val userVM = UserVM(userRepository)
    private var navigator: Navigator? = null
    private var currentUser = NONE

    @get:Rule
    val rule = createComposeRule()

    @BeforeTest
    fun setUp() {

        rule.mainClock.autoAdvance = true
    }

    @AfterTest
    fun tearDown() {
        runBlocking(Dispatchers.IO) {
            servicesRepository.search(currentUser).forEach {
                servicesRepository.delete(it.service, it.userid)
            }
        }
        currentUser = NONE
    }

    @Test
    fun shouldShowLogin() = runTest {
        rule.setContent {
            withLogin(null) {
                withDI(di) {
                    withNavigator {
                        app()
                    }
                }
            }
        }
        rule.awaitIdle()
        performLogin()
        rule.awaitIdle()
        rule.onNodeWithTag("Search field").assertExists()
    }

    @Test
    fun shouldShowAnErrorIfLoginFail() = runTest {
        rule.setContent {
            withDI(di) {
                withNavigator {
                    app()
                }
            }
        }

        performLogin("wrong user")
        rule.awaitIdle()
        rule.onNodeWithTag("Login error msg").assertExists().assertTextContains("Invalid credentials")
    }


    @Test
    fun `should be able to add a service`() = runTest {

        rule.setContent {
            withLogin(null) {
                withDI(di) {
                    withNavigator {
                        app()
                    }
                }
            }
        }
        rule.awaitIdle()
        performLogin()
        val service = Service(
            service = "myService",
            username = "a username",
            password = "a password",
            tags = setOf("tag"),
            note = "someNote",
            userid = user.userid,
            dirty = true,
            score = 0.0,
            updateTime = clock.currentDateTime()
        )
        rule.awaitIdle()
        rule.onNodeWithTag("Search field").assertExists()
        navigator?.push(KPasswordScreen.NewService)
        insertService(service)
        rule.awaitIdle()
        await.atMost(Duration.ofSeconds(1)).until {
            servicesRepository.search(currentUser, "").size == 1
        }
        rule.awaitIdle()
        await.atMost(Duration.ofSeconds(1)).untilAsserted {
            rule.onNodeWithTag("Search field").assertExists()
        }
        rule.waitUntilNodeCount(hasText(service.service), 1, 3000)
    }

    @Test
    fun `should throw an error if service is add two times`() = runTest {
        rule.setContent {
            withLogin(null) {
                withDI(di) {
                    withNavigator {
                        app()
                    }
                }
            }
        }
        rule.awaitIdle()
        performLogin()
        val service = Service(
            service = "myService",
            username = "a username",
            password = "a password",
            tags = setOf("tag"),
            note = "someNote",
            userid = user.userid,
            dirty = true,
            score = 0.0,
            updateTime = clock.currentDateTime()
        )
        rule.awaitIdle()
        rule.onNodeWithTag("Search field").assertExists()
        navigator?.push(KPasswordScreen.NewService)
        insertService(service)
        rule.awaitIdle()
        await.atMost(Duration.ofSeconds(1)).until {
            //waiting that the system is able to read the new data, if not the visualization is flaky
            servicesRepository.search(currentUser, "").size == 1
        }
        rule.onNodeWithTag("Search field").assertExists()
        navigator?.push(KPasswordScreen.NewService)
        fillService(service)
        rule.waitUntilAtLeastOneExists(hasTestTag("ErrorMsg"))
    }

    @Test
    fun printProperties() {
        System.getProperties().forEach { key, value ->
            LOGGER.info { "$key:$value" }
        }
    }

    @Test
    fun `should show all the inserted services`() = runTest(timeout = 20.seconds) {

        rule.setContent {
            withLogin(null) {
                withDI(di) {
                    withNavigator {
                        app()
                    }
                }
            }
        }
        rule.awaitIdle()
        performLogin()
        rule.awaitIdle()
        val services = mutableListOf<Service>()
        (1..5).forEach {

            val service = Service(
                service = faker.app().name() + it,
                username = faker.dragonBall().character(),
                password = faker.dune().planet(),
                tags = setOf(faker.book().genre()),
                note = faker.dune().quote(),
                userid = user.userid,
                dirty = true,
                score = 0.0,
                updateTime = clock.currentDateTime()
            )
            rule.onNodeWithTag("Search field").assertExists()
            navigator?.push(KPasswordScreen.NewService)
            insertService(service)
            rule.awaitIdle()

            await.atMost(Duration.ofSeconds(1)).until {
                //waiting that the system is able to read the new data, if not the visualization is flaky
                val loadedServices = servicesRepository.search(currentUser)
                loadedServices.size == it
            }
            services.add(service)
        }
        rule.onNodeWithTag("Search field").assertExists()
        LOGGER.warn { "Check if service are displayed" }


        val service = services[2]
        LOGGER.warn { "Try to edit a service" }
        rule.awaitIdle()
        await.atMost(Duration.ofSeconds(1)).untilAsserted {
            // the display has a delay for slow down typing, this impact on testing.
            rule.onNodeWithTag("Edit ${service.service}").assertExists().performClick()
        }

        System.err.println("Waiting for the service")
        rule.waitUntilAtLeastOneExists(hasTestTag("service"))
        rule.onNodeWithTag("service").assertTextContains(service.service)
    }

    private suspend fun insertService(service: Service) {
        //this is an ugly workaround, but navigate in the menu is a nightmare
        fillService(service)
        rule.waitUntil(timeoutMillis = 1000) {
            runBlocking {
                servicesRepository.search(currentUser, "", setOf()).any {
                    it.service == service.service
                }
            }
        }

        rule.waitUntilAtLeastOneExists(hasTestTag("Search field"))
    }

    private suspend fun fillService(service: Service) {


        rule.waitUntilExactlyOneExists(hasTestTag("service"), 3000)

        rule.onNodeWithTag("service").performSemanticsAction(RequestFocus)
        rule.waitUntilAtLeastOneExists(hasTestTag("service") and isFocused())
        rule.onNodeWithTag("service").performTextInput(service.service)
        rule.onNodeWithTag("username").performTextInput(service.username)
        rule.onNodeWithTag("password").performTextInput(service.password)
        rule.onNodeWithTag("tags").performTextInput(service.tags.first())
        rule.onNodeWithTag("note").performTextInput(service.note)
        rule.awaitIdle()

        rule.onNodeWithTag("submit").performClick()
    }

    private suspend fun performLogin(username: String = "dummy") {
        rule.awaitIdle()
        rule.onNodeWithTag("username").assertExists().performTextInput(username)
        rule.awaitIdle()
        rule.onNodeWithTag("password").assertExists().performTextInput("secret")
        rule.awaitIdle()
        rule.onNodeWithTag("login").assertExists().performClick()
        currentUser = runCatching {
            userRepository.findUser(username)
        }.getOrDefault(NONE)
    }

    @Composable
    fun withNavigator(
        screen: Screen = KPasswordScreen.Login,
        content: @Composable () -> Unit
    ) {
        Navigator(screen) {
            navigator = it
            val snackbarHostState = remember { SnackbarHostState() }
            val drawerState = remember { DrawerState(DrawerValue.Closed) }
            CompositionLocalProvider(
                LocalStatusHolder provides StatusHolder(snackbarHostState, drawerState)
            ) {
                content()
            }
        }
    }
}
