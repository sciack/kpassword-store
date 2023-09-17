package passwordStore.widget

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import passwordStore.navigation.KPasswordScreen
import passwordStore.ui.theme.XL
import passwordStore.ui.theme.XXL
import passwordStore.utils.LocalStatusHolder

@Composable
fun confirmUpload(text: String) {
    val statusHolder = LocalStatusHolder.currentOrThrow
    val navigator = LocalNavigator.currentOrThrow
    val coroutineScope = rememberCoroutineScope()

    Row(Modifier.fillMaxSize()) {
        Icon(Icons.Default.Info, "Success", Modifier.size(XL * 2).align(Alignment.CenterVertically))
        Spacer(Modifier.width(XXL))
        Text(
            text,
            style = MaterialTheme.typography.h5,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
    LaunchedEffect("CSV processing confirmation") {
        coroutineScope.launch {
            statusHolder.sendMessage(text)
            navigator.popUntil { it == KPasswordScreen.Home }
        }
    }
}