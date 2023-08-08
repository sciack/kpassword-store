package passwordStore

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import mu.KotlinLogging

@Composable
fun loginPane(loginFunction: LoginFunction) {
    val username = remember {
        mutableStateOf(TextFieldValue())
    }
    val password = remember {
        mutableStateOf(TextFieldValue())
    }

    val failed = remember {
        mutableStateOf(false)
    }
    Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {
        OutlinedTextField(
            label = { Text("Username") },
            value = username.value,
            singleLine = true,
            onValueChange = { username.value = it },
            isError = failed.value,
            modifier = Modifier.focusable(true).align(Alignment.CenterHorizontally).testTag("username"),
        )
        OutlinedTextField(
            label = { Text("Password") },
            value = password.value,
            singleLine = true,
            onValueChange = { password.value = it },
            visualTransformation = PasswordVisualTransformation(),
            isError = failed.value,
            modifier = Modifier.focusable(true).align(Alignment.CenterHorizontally).testTag("password")
        )
        if (failed.value) {
            Text(
                "Invalid credentials",
                modifier = Modifier.align(Alignment.CenterHorizontally).testTag("Login error msg"),
                color = MaterialTheme.colors.error,
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(0.8f, TextUnitType.Em)
            )
        }
        Button(
            onClick = {
                loginFunction(username.value, password.value).onFailure {
                    failed.value = true
                }.onSuccess {
                    failed.value = false
                }
            },
            modifier = Modifier.focusable(true).align(Alignment.CenterHorizontally).testTag("login")
        ) {
            Text("Login")
        }

    }
}

