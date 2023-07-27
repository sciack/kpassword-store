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
import org.kodein.di.compose.localDI
import org.kodein.di.instance
import passwordStore.services.ServiceViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
fun tagView() {
    val serviceModel by localDI().instance<ServiceViewModel>()
    val tags = remember {
        serviceModel.tags
    }
    val selected = remember {
        mutableStateOf("")
    }

    FlowRow(
        maxItemsInEachRow = 15,
        modifier = Modifier.fillMaxWidth().focusable(false),

        ) {
        tags.value.forEach { (tag, _) ->

            Chip(
                onClick = {
                    if (selected.value != tag) {
                        selected.value = tag
                    } else {
                        selected.value = ""
                    }
                    serviceModel.searchWithTags(selected.value)
                },
                colors = if (selected.value != tag) {
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
