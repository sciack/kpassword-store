package passwordStore.widget

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun ColumnScope.menuItem(
    icon: @Composable () -> Unit,
    title: String,
    testTag: String = "",
    selected: Boolean = false,
    onClick: () -> Unit
) {

    NavigationDrawerItem(
        onClick = onClick,
        icon = icon,
        label = {
            Text(title)
        },
        selected = selected,
        modifier = Modifier.testTag(testTag)
    )
}