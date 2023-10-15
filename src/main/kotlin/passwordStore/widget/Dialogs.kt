package passwordStore.widget

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.font.FontWeight
import kotlin.random.Random

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

