package passwordStore.utils

import androidx.compose.material3.DrawerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatusHolder(val snackbarHostState: SnackbarHostState, val drawerState: DrawerState) {

    suspend fun sendMessage(message: String) {
        withContext(Dispatchers.Default) {
            snackbarHostState.showSnackbar(message)

        }
    }

    suspend fun openDrawer() {
        withContext(Dispatchers.Main) {
            if (drawerState.isClosed) {
                drawerState.open()
            }
        }
    }

    suspend fun closeDrawer() {
        withContext(Dispatchers.Main) {
            if (drawerState.isOpen) {
                drawerState.close()
            }
        }
    }
}

val LocalStatusHolder: ProvidableCompositionLocal<StatusHolder?> =
    staticCompositionLocalOf { null }