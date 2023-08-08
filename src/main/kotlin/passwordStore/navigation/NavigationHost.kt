package passwordStore.navigation

import androidx.compose.runtime.Composable
import org.kodein.di.compose.localDI
import org.kodein.di.instance
import passwordStore.Screen
import passwordStore.services.ServiceVM
import kotlin.reflect.KClass

/**
 * NavigationHost class
 */
class NavigationHost(
    val navController: NavController,
    val contents: @Composable NavigationGraphBuilder.() -> Unit
) {

    @Composable
    fun build() {
        NavigationGraphBuilder().renderContents()
    }

    inner class NavigationGraphBuilder(
        val navController: NavController = this@NavigationHost.navController
    ) {
        @Composable
        fun renderContents() {
            this@NavigationHost.contents(this)
        }
    }
}


/**
 * Composable to build the Navigation Host
 */
@Composable
fun NavigationHost.NavigationGraphBuilder.composable(
    route: Screen,
    content: @Composable () -> Unit
) {
    if (navController.currentScreen.value == route) {
        content()
    }

}

@Composable
fun NavigationHost.NavigationGraphBuilder.composable(
    route: KClass<out Screen>,
    content: @Composable () -> Unit
) {
    if (navController.currentScreen.value::class == route) {
        content()
    }

}

@Composable
fun NavigationHost.NavigationGraphBuilder.authenticatedComposable(
    route: KClass<out Screen>,
    content: @Composable () -> Unit
) {
    if (navController.currentScreen.value::class == route) {
        val serviceModel by localDI().instance<ServiceVM>()
        check(serviceModel.user.value.id > 0) {
            "Access denied"
        }
        content()
    }
}


@Composable
fun NavigationHost.NavigationGraphBuilder.authenticatedComposable(
    route: Screen,
    content: @Composable () -> Unit
) {
    if (navController.currentScreen.value == route) {
        val serviceModel by localDI().instance<ServiceVM>()
        check(serviceModel.user.value.id > 0) {
            "Access denied"
        }
        content()
    }

}