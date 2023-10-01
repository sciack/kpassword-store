package passwordStore.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import passwordStore.ui.theme.XL
import passwordStore.ui.theme.XS

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
            .padding(top = XS, bottom = XS)
            .fillMaxWidth()
            .then(modifier)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.testTag(testTag)
                .align(Alignment.CenterVertically)
                .size(XL)
        ) {
            icon()
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.clickable {
                onClick()
            }.align(Alignment.CenterVertically)
        )
    }
}