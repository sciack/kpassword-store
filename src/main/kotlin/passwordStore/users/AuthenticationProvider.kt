package passwordStore.users

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf


val LocalUser: ProvidableCompositionLocal<User?> =
    compositionLocalOf { null }
val LocalSetUser: ProvidableCompositionLocal<(User?) -> Unit> = compositionLocalOf { {} }


