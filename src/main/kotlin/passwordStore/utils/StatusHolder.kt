package passwordStore.utils

import androidx.compose.material.ScaffoldState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object StatusHolder {

    lateinit var scaffoldState: ScaffoldState

    suspend fun sendMessage(message: String) {
        if (::scaffoldState.isInitialized) {
            withContext(Dispatchers.Default) {
                scaffoldState.snackbarHostState.showSnackbar(message)

            }
        }
    }

    suspend fun openDrawer() {
        if (::scaffoldState.isInitialized) {
            withContext(Dispatchers.Default) {
                if (scaffoldState.drawerState.isClosed) {
                    scaffoldState.drawerState.open()
                }
            }
        }
    }

    suspend fun closeDrawer() {
        if (::scaffoldState.isInitialized) {
            withContext(Dispatchers.Default) {
                if (scaffoldState.drawerState.isOpen) {
                    scaffoldState.drawerState.close()
                }
            }
        }
    }
}