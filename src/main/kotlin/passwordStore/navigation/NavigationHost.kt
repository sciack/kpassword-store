package passwordStore.navigation

import androidx.compose.runtime.Composable
import passwordStore.Screen
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