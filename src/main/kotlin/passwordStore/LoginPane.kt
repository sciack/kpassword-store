package passwordStore

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import passwordStore.ui.theme.MEDIUM
import passwordStore.ui.theme.SMALL
import passwordStore.ui.theme.XXL
import passwordStore.users.User

typealias LoginFunction = (TextFieldValue, TextFieldValue) -> Result<User>

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
    val coroutineScope = rememberCoroutineScope()

    val onLogin = {
        coroutineScope.launch {
            loginFunction(username.value, password.value).onFailure {
                failed.value = true
            }.onSuccess {
                failed.value = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        OutlinedCard  (
            modifier = Modifier.align(Alignment.Center),
            //elevation = CardDefaults.cardElevation(defaultElevation = SMALL)
        ) {
            Text("Login",
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.secondary,
                modifier = Modifier.padding(MEDIUM),
            )
            Column(Modifier.padding(XXL), Arrangement.spacedBy(5.dp)) {
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
                    modifier = Modifier.focusable(true).align(Alignment.CenterHorizontally).testTag("password").onKeyEvent {
                        event ->
                        if(event.key == Key.Enter) {
                            onLogin()
                            false
                        } else {
                            true
                        }
                    }
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
                Spacer(Modifier.width(MEDIUM))
                Button(
                    onClick = {
                        onLogin()
                    },
                    modifier = Modifier.focusable(true).align(Alignment.CenterHorizontally).testTag("login")
                ) {
                    Text("Login")
                }
            }
        }

    }
}

