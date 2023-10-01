package passwordStore.widget

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
        Icon(Icons.Default.Error, "Error", Modifier.size(XL * 2).align(Alignment.Top))
        Spacer(Modifier.width(XL))
        Text(
            errorMsg,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.align(Alignment.Top)
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


@Composable
fun confirmUpload(text: String, afterEffect: suspend () -> Unit = {}) {


    Row(Modifier.fillMaxSize()) {
        Icon(Icons.Default.Info, "Success", Modifier.size(XL * 2).align(Alignment.Top))
        Spacer(Modifier.width(XXL))
        Text(
            text,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Top)
        )
    }
    LaunchedEffect("CSV processing confirmation") {
        afterEffect()
    }
}