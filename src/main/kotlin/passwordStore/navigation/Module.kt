package passwordStore.navigation

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import passwordStore.Screen

internal val navigation = DI.Module("Navigation") {
    bind<NavController> {
        singleton {
            NavController(Screen.Login)
        }
    }
}