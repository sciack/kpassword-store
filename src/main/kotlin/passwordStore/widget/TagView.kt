package passwordStore.widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import mu.KotlinLogging
import org.kodein.di.compose.localDI
import org.kodein.di.instance
import passwordStore.Services

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
fun tagView() {
    val serviceModel by localDI().instance<Services>()
    val tags = remember {
        serviceModel.tags
    }
    val selected = remember {
        mutableStateOf<String>("")
    }

    FlowRow(
        maxItemsInEachRow = 10,
        modifier = Modifier.fillMaxWidth().focusable(false),

        ) {
        tags.value.forEach { (tag, frequency) ->

            Chip(
                onClick = {
                    LOGGER.warn { "Click called for tag $tag" }
                    if (selected.value != tag) {
                        serviceModel.search(tag)
                        selected.value = tag
                    } else {
                        serviceModel.fetchAll()
                        selected.value = ""
                    }
                },
                colors = if(selected.value != tag) {
                    ChipDefaults.outlinedChipColors()
                } else {
                    ChipDefaults.chipColors()
                },
                border = BorderStroke(1.dp, Color.Blue)
            ) {
                Text(tag)
            }
        }
    }
}

private val LOGGER = KotlinLogging.logger { }