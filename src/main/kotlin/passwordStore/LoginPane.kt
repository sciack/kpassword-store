package passwordStore

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.VisualTransformation
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
            modifier = Modifier.align(Alignment.Center).padding(XXL),
            //elevation = CardDefaults.cardElevation(defaultElevation = SMALL)
        ) {
            Text("Login",
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.secondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(XXL),
            )
            Column(Modifier.padding(start = XXL, end= XXL, bottom = XXL), Arrangement.spacedBy(5.dp)) {
                OutlinedTextField(
                    label = { Text("Username") },
                    value = username.value,
                    singleLine = true,
                    onValueChange = { username.value = it },
                    isError = failed.value,
                    modifier = Modifier.align(Alignment.CenterHorizontally).testTag("username"),
                )
                val (passwordVisualTransformation, setVisualTransformation) = remember {
                    mutableStateOf<VisualTransformation>(PasswordVisualTransformation())
                }
                OutlinedTextField(
                    label = { Text("Password") },
                    placeholder = { Text("Password")},
                    value = password.value,
                    singleLine = true,
                    onValueChange = { password.value = it },
                    visualTransformation = passwordVisualTransformation,
                    isError = failed.value,
                    modifier = Modifier.align(Alignment.CenterHorizontally).testTag("password").onKeyEvent {
                        event ->
                        if(event.key == Key.Enter) {
                            onLogin()
                            false
                        } else {
                            true
                        }
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                setVisualTransformation( if(passwordVisualTransformation == VisualTransformation.None ) {
                                    PasswordVisualTransformation()
                                } else {
                                    VisualTransformation.None
                                }
                                )
                            }
                        ) {
                            val icon = if (passwordVisualTransformation == VisualTransformation.None) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            }
                            Icon(icon, null)
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

