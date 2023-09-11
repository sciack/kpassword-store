package passwordStore.widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.currentOrThrow
import passwordStore.tags.Tag
import passwordStore.ui.theme.XS
import passwordStore.users.LocalUser

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
fun tagView(inputTag: List<Tag>, selectedTag: Tag?, searchFn: (Tag?) -> Unit) {
    val tags = remember {
        inputTag
    }
    val selected = remember {
        mutableStateOf(selectedTag)
    }

    FlowRow(
        maxItemsInEachRow = 12,
        modifier = Modifier
            .focusable(false)
            .padding(top = XS, bottom = XS)
            .border(width = 1.dp, color = MaterialTheme.colors.primary),

        ) {
        tags.forEach { tag ->

            Chip(
                onClick = {
                    if (selected.value != tag) {
                        selected.value = tag
                    } else {
                        selected.value = null
                    }
                    searchFn(selected.value)
                },
                colors = if (selected.value != tag) {
                    ChipDefaults.outlinedChipColors()
                } else {
                    ChipDefaults.chipColors()
                },
                border = BorderStroke(1.dp, MaterialTheme.colors.primary),
                modifier = Modifier.padding(top = XS, start = XS, end = XS, bottom = XS)
            ) {
                Text(tag.name)
            }
        }
    }
}
