package passwordStore.widget

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
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
import passwordStore.tags.Tag
import passwordStore.ui.theme.MEDIUM
import passwordStore.ui.theme.XS

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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
                InputChip(onClick = {
                    tags.value -= value
                    onValueChange(tags.value)
                }, leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        "Delete",
                    )
                },
                    modifier = Modifier.padding(top = XS, start = 0.dp, end = XS),
                    label = {
                        Text(value)
                    },
                    selected = false
                )
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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
                FilterChip(
                    onClick = {},
                    modifier = Modifier.padding(top = XS, start = 0.dp, end = XS),
                    label = {
                        Text(value)
                    },
                    selected = false
                )
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun tagView(tags: Set<Tag>, selectedTag: Set<Tag>, searchFn: (Set<Tag>) -> Unit) {

    ElevatedCard {
        FlowRow(
            maxItemsInEachRow = 12,
            modifier = Modifier
                .focusable(false)
                .padding(MEDIUM),

            ) {
            tags.forEach { tag ->
                FilterChip(
                    onClick = {

                        val selected = if (tag !in selectedTag) {
                            selectedTag + tag
                        } else {
                            selectedTag - tag
                        }
                        searchFn(selected)
                    },
                    modifier = Modifier.padding(top = XS, start = XS, end = XS, bottom = XS),
                    selected = tag in selectedTag,
                    label = {
                        Text(tag.name)
                    }
                )
            }
        }
    }
}
