package passwordStore.widget

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun showOkCancel(
    title: String = "Alert", message: String, show: MutableState<Boolean>, onConfirm: () -> Unit = {}
) {

    if (show.value) {
        AlertDialog(text = {
            Text(message)
        }, onDismissRequest = {}, title = {
            Text(
                title, fontWeight = FontWeight.Bold
            )
        }, dismissButton = {
            Button(onClick = {
                show.value = false
            }) {
                Text("Cancel")
            }
        }, confirmButton = {
            Button(onClick = {
                show.value = false
                onConfirm()
            }) {
                Text("OK")
            }
        })
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun showOk(
    title: String = "Error", message: String, show: MutableState<Boolean>
) {

    if (show.value) {
        AlertDialog(text = {
            Text(message)
        }, onDismissRequest = {}, title = {
            Text(
                title, fontWeight = FontWeight.Bold
            )
        }, confirmButton = {
            Button(onClick = {
                show.value = false
            }) {
                Text("OK")
            }
        })
    }

}