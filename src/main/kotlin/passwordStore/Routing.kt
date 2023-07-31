package passwordStore

import passwordStore.services.Service

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

    object Settings: Screen {
        override val name: String
            get() = "Settings"

        override val allowBack: Boolean
            get() = true
    }
}