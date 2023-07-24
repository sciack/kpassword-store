package passwordStore

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {  }

@Composable
fun loginPane(loginFunction: LoginFunction) {
    val username = remember {
        mutableStateOf(TextFieldValue())
    }
    val password = remember {
        mutableStateOf(TextFieldValue())
    }

    Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            OutlinedTextField(
                label = { Text("Username") },
                value = username.value,
                singleLine = true,
                onValueChange = { username.value = it },
                modifier = Modifier.focusable(true),
            )
        }
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            OutlinedTextField(
                label = { Text("Password") },
                value = password.value,
                singleLine = true,
                onValueChange = { password.value = it },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.focusable(true)
            )
        }
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Button(onClick = {
                loginFunction(username.value, password.value).onFailure {
                    LOGGER.warn(it) {"Error processing login"}
                }
            },
                modifier = Modifier.focusable(true)) {
                Text("Login")
            }
        }
    }
}

