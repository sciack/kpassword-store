package passwordStore.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun menuItem(
    icon: @Composable () -> Unit,
    title: String,
    testTag: String = "",
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(top = 4.dp, bottom = 4.dp)
            .fillMaxWidth()
            .then(modifier)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.testTag(testTag)
                .align(Alignment.CenterVertically)
                .size(24.dp)
        ) {
            icon()
        }
        Text(
            text = title,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.clickable {
                onClick()
            }.align(Alignment.CenterVertically)
        )
    }
}