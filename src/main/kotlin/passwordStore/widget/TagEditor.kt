package passwordStore.widget

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
fun tagEditor(tags: MutableState<Set<String>>, onValueChange: (Set<String>) -> Unit) {
    val currentTag = remember {
        mutableStateOf(TextFieldValue())
    }

    Column(Modifier.fillMaxWidth()) {
        FlowRow(
            maxItemsInEachRow = 10,
            modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally).focusable(false),

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
                    border = BorderStroke(1.dp, MaterialTheme.colors.primary)
                ) {
                    Text(value)
                }
                Spacer(Modifier.width(4.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
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
fun TagViewer(tags: MutableState<Set<String>>) {

    Column(Modifier.fillMaxWidth()) {
        FlowRow(
            maxItemsInEachRow = 10,
            modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally).focusable(false),

            ) {
            tags.value.forEach { value ->
                Chip(
                    onClick = {},
                    colors = ChipDefaults.outlinedChipColors(),
                    border = BorderStroke(1.dp, MaterialTheme.colors.primary)
                ) {
                    Text(value)
                }
                Spacer(Modifier.width(4.dp))
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