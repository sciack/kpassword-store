package passwordStore

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {  }

@Composable
fun loginPane(username: MutableState<TextFieldValue>, password: MutableState<TextFieldValue>,
              loginFunction: LoginFunction) {
    Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Username:", Modifier.align(Alignment.CenterVertically))
            Spacer(modifier = Modifier.width(10.dp))
            TextField(
                label = { Text("Username") },
                value = username.value,
                onValueChange = { username.value = it }
            )
        }
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Password:", Modifier.align(Alignment.CenterVertically))
            Spacer(modifier = Modifier.width(10.dp))
            TextField(
                label = { Text("Password") },
                value = password.value,
                onValueChange = { password.value = it },
                visualTransformation = PasswordVisualTransformation()
            )
        }
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Button(onClick = {
                loginFunction(username.value, password.value).onFailure {
                    LOGGER.warn(it) {"Error processing login"}
                }
            }) {
                Text("Login")
            }
        }
    }
}

