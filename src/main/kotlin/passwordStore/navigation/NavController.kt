package passwordStore.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import org.kodein.di.compose.localDI
import org.kodein.di.instance
import passwordStore.Screen

/**
 * NavController Class
 */
class NavController(
    private val startDestination: Screen,
    private var backStackScreens: MutableSet<Screen> = mutableSetOf()
) {
    // Variable to store the state of the current screen
    var currentScreen: MutableState<Screen> = mutableStateOf(startDestination)

    // Function to handle the navigation between the screen
    fun navigate(route: Screen) {
        if (route != currentScreen.value) {
            if (backStackScreens.contains(currentScreen.value) && currentScreen.value != startDestination) {
                backStackScreens.remove(currentScreen.value)
            }

            if (route == startDestination) {
                backStackScreens = mutableSetOf()
            } else {
                backStackScreens.add(currentScreen.value)
            }

            currentScreen.value = route
        }
    }

    // Function to handle the back
    fun navigateBack() {
        if (backStackScreens.isNotEmpty()) {
            currentScreen.value = backStackScreens.last()
            backStackScreens.remove(currentScreen.value)
        }
    }
}


/**
 * Composable to remember the state of the navcontroller
 */
@Composable
fun rememberNavController(
): MutableState<NavController> {
    val navController by localDI().instance<NavController>()
    return rememberSaveable {
        mutableStateOf(navController)
    }
}