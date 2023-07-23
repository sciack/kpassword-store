package passwordStore

sealed interface Screen {
    open val name: String

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
    }

    object NewService: Screen {
        override val name: String
            get() = "New Service"
    }
}