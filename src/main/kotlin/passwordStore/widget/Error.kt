package passwordStore.widget

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalMapOf
import passwordStore.ui.theme.XL
import passwordStore.ui.theme.XXL

@Composable
fun error(errorMsg: String) {

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

}