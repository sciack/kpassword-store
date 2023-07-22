package passwordStore

import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize

sealed class Screen : Parcelable {
    @Parcelize
    object List : Screen()

    @Parcelize
    object Login : Screen()

    @Parcelize
    data class Details(val service: Service) : Screen()

    @Parcelize
    object NewService: Screen()
}