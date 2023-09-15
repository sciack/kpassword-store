package passwordStore.widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import passwordStore.ui.theme.XS
import passwordStore.tags.Tag

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
fun tagEditor(tags: MutableState<Set<String>>, onValueChange: (Set<String>) -> Unit) {
    val currentTag = remember {
        mutableStateOf(TextFieldValue())
    }

    Column(Modifier.fillMaxWidth()) {
        FlowRow(
            maxItemsInEachRow = 10,
            modifier = Modifier.fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .focusable(false)
                .padding(top = XS, bottom = XS),

            ) {
            tags.value.forEach { value ->
                Chip(onClick = {
                    tags.value -= value
                    onValueChange(tags.value)
                }, leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        "Delete",
                    )
                },
                    colors = ChipDefaults.outlinedChipColors(),
                    border = BorderStroke(1.dp, MaterialTheme.colors.primary),
                    modifier = Modifier.padding(top = XS, start = 0.dp, end = XS)
                ) {
                    Text(value)
                }

            }
        }
        OutlinedTextField(
            label = { Text("Tags") },

            singleLine = true,
            value = currentTag.value,
            onValueChange = {
                if (it.text.isNotEmpty() &&
                    it.text.last() in arrayOf(',', '.', ';', ' ', '\n')
                ) {
                    assignTag(tags, currentTag, it.text.dropLast(1), onValueChange)
                } else {
                    currentTag.value = it
                }
            },
            modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally).onFocusChanged { focusState ->
                if (!focusState.hasFocus && currentTag.value.text.isNotEmpty()) {
                    assignTag(tags, currentTag, currentTag.value.text, onValueChange)
                }
            }.testTag("tags")
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
fun tagViewer(tags: MutableState<Set<String>>, tagsPerRow: Int = 10) {

    Column(Modifier.fillMaxWidth()) {
        FlowRow(
            maxItemsInEachRow = tagsPerRow,
            modifier = Modifier.fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .focusable(false)
                .padding(top = XS, bottom = XS),

            ) {
            tags.value.forEach { value ->
                Chip(
                    onClick = {},
                    colors = ChipDefaults.outlinedChipColors(),
                    border = BorderStroke(1.dp, MaterialTheme.colors.primary),
                    modifier = Modifier.padding(top = XS, start = 0.dp, end = XS)
                ) {
                    Text(value)
                }
                Spacer(Modifier.width(XS))
            }
        }

    }
}


private fun assignTag(
    tags: MutableState<Set<String>>,
    currentTag: MutableState<TextFieldValue>,
    tag: String,
    onValueChange: (Set<String>) -> Unit
) {
    tags.value += tag
    currentTag.value = TextFieldValue()
    onValueChange(tags.value)
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
fun tagView(tags: Set<Tag>, selectedTag: Tag?, searchFn: (Tag?) -> Unit) {
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
