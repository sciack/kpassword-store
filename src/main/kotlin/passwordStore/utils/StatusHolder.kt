package passwordStore.utils

import androidx.compose.material.DrawerState
import androidx.compose.material.ScaffoldState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object StatusHolder {

    lateinit var scaffoldState: ScaffoldState

    lateinit var drawerState: DrawerState

    suspend fun sendMessage(message: String) {
        if (::scaffoldState.isInitialized) {
            withContext(Dispatchers.Default) {
                    scaffoldState.snackbarHostState.showSnackbar(message)

            }
        }
    }
}