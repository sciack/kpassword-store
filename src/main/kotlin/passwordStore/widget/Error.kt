package passwordStore.widget

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import passwordStore.navigation.KPasswordScreen
import passwordStore.ui.theme.XL
import passwordStore.ui.theme.XXL
import passwordStore.utils.LocalStatusHolder

@Composable
fun error(errorMsg: String, afterEffect: suspend () -> Unit = {}) {

    Row(Modifier.padding(XXL).fillMaxSize()) {
        Icon(Icons.Default.Error, "Error", Modifier.size(XL * 2).align(Alignment.CenterVertically))
        Spacer(Modifier.width(XL))
        Text(
            errorMsg,
            style = MaterialTheme.typography.h5,
            color = MaterialTheme.colors.error,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
    LaunchedEffect("CSV processing confirmation") {
        afterEffect()
    }
}


@Composable
fun displayError(errorMsg: String) {
    val statusHolder = LocalStatusHolder.currentOrThrow
    val navigator = LocalNavigator.currentOrThrow
    error(errorMsg) {
        statusHolder.sendMessage(errorMsg)
        navigator.popUntil { it == KPasswordScreen.Home }
    }
}