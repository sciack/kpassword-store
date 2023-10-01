package passwordStore.widget

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import passwordStore.ui.theme.MEDIUM
import passwordStore.utils.obfuscate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun passwordToolTip(password: String, color: Color = MaterialTheme.colorScheme.onBackground) {
    val newPwd = remember { password }
    TooltipArea(tooltip = {
        Row(
            Modifier.background(MaterialTheme.colorScheme.surface)
                .border(2.dp, color = MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(4.dp))
        ) {
            Text(
                newPwd,
                modifier = Modifier.padding(MEDIUM)
            )
        }
    }) {
        Text(newPwd.obfuscate(), color = color)
    }
}