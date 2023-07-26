package passwordStore

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger { }

@Composable
fun loginPane(loginFunction: LoginFunction) {
    val username = remember {
        mutableStateOf(TextFieldValue())
    }
    val password = remember {
        mutableStateOf(TextFieldValue())
    }

    val passwordVisible = remember {
        mutableStateOf(false)
    }
    Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {
            OutlinedTextField(
                label = { Text("Username") },
                value = username.value,
                singleLine = true,
                onValueChange = { username.value = it },
                modifier = Modifier.focusable(true).align(Alignment.CenterHorizontally),
            )
            OutlinedTextField(
                label = { Text("Password") },
                value = password.value,
                singleLine = true,
                onValueChange = { password.value = it },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.focusable(true).align(Alignment.CenterHorizontally)
            )
            Button(
                onClick = {
                    loginFunction(username.value, password.value).onFailure {
                        LOGGER.warn(it) { "Error processing login" }
                    }
                },
                modifier = Modifier.focusable(true).align(Alignment.CenterHorizontally)
            ) {
                Text("Login")
            }

    }
}

