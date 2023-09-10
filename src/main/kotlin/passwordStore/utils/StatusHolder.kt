package passwordStore.utils

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatusHolder(val scaffoldState: ScaffoldState) {

    suspend fun sendMessage(message: String) {
        withContext(Dispatchers.Default) {
            scaffoldState.snackbarHostState.showSnackbar(message)

        }
    }

    suspend fun openDrawer() {
        withContext(Dispatchers.Default) {
            if (scaffoldState.drawerState.isClosed) {
                scaffoldState.drawerState.open()
            }
        }
    }

    suspend fun closeDrawer() {
        withContext(Dispatchers.Default) {
            if (scaffoldState.drawerState.isOpen) {
                scaffoldState.drawerState.close()
            }
        }
    }
}

val LocalStatusHolder: ProvidableCompositionLocal<StatusHolder?> =
    staticCompositionLocalOf { null }