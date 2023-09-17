package passwordStore.widget

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import passwordStore.ui.theme.XL
import passwordStore.ui.theme.XXL

@Composable
fun confirmUpload(text: String, afterEffect: suspend () -> Unit = {}) {


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
       afterEffect()
    }
}