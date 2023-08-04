package passwordStore.users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.kodein.di.compose.rememberInstance
import passwordStore.Screen
import passwordStore.navigation.NavController
import passwordStore.services.ServiceViewModel
import passwordStore.widget.bottomBorder
import passwordStore.widget.passwordDialog

@Composable
fun userSettings(currentUser: User) {

    val user = remember {
        mutableStateOf(
            UpdateUser(
                userid = currentUser.userid,
                email = currentUser.email,
                fullName = currentUser.fullName,
                password = "",
                roles = currentUser.roles
            )
        )
    }

    val dirty = remember {
        mutableStateOf(false)
    }

    val passwordConfirmation = remember {
        mutableStateOf(TextFieldValue())
    }

    val userRepository by rememberInstance<UserRepository>()
    val serviceViewModel by rememberInstance<ServiceViewModel>()
    val navController by rememberInstance<NavController>()
    val showPasswordDialog = remember {
        mutableStateOf(false)
    }

    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = user.value.fullName,
            onValueChange = { name ->
                user.value = user.value.copy(fullName = name)
                dirty.value = true
            },
            label = {
                Text("Fullname")
            },
            modifier = Modifier.align(Alignment.CenterHorizontally).testTag("fullName")
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            label = {
                Text("Email")
            },
            value = user.value.email,

            onValueChange = { email ->
                user.value = user.value.copy(email = email)
                dirty.value = true
            },
            modifier = Modifier.align(Alignment.CenterHorizontally).testTag("email")
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            label = {
                Text("Password")
            },
            value = user.value.password,
            trailingIcon = {
                IconButton(onClick = { showPasswordDialog.value = showPasswordDialog.value.not() }) {
                    Icon(Icons.Default.Edit, "Generate password")
                }
            },
            visualTransformation = PasswordVisualTransformation(),
            onValueChange = { password ->
                user.value = user.value.copy(password = password)
                dirty.value = true
            },
            isError = passwordConfirmation.value.text != user.value.password,
            modifier = Modifier.align(Alignment.CenterHorizontally).testTag("password")
        )
        Spacer(Modifier.height(12.dp))
        Card (Modifier.align(Alignment.CenterHorizontally).padding(4.dp)) {
            passwordDialog(showPasswordDialog) {
                user.value = user.value.copy(password = it)
                passwordConfirmation.value = TextFieldValue(it)
                dirty.value = true
            }
        }

        OutlinedTextField(
            label = {
                Text("Password confirmation")
            },
            value = passwordConfirmation.value,
            visualTransformation = PasswordVisualTransformation(),
            onValueChange = { password ->
                passwordConfirmation.value = password
                dirty.value = true
            },
            isError = passwordConfirmation.value.text != user.value.password,
            modifier = Modifier.align(Alignment.CenterHorizontally).testTag("password-confirmation")
        )
        Spacer(Modifier.height(16.dp))
        if (currentUser.isAdmin()) {
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Column {
                    Text("Roles:")

                    Roles.values().forEach {role ->
                        Row {
                            Checkbox(checked = user.value.roles.contains(role),
                                onCheckedChange = {value ->
                                    val roles = user.value.roles.toMutableSet()
                                    if (value) {
                                        roles.add(role)
                                    } else {
                                        roles.remove(role)
                                    }
                                    user.value = user.value.copy(roles = roles)
                                }
                                )
                            Text(
                                text = role.name,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                            Spacer(Modifier.width(16.dp))
                        }
                    }
                }

            }
        } else {
            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Roles:")
                currentUser.roles.forEach {
                    Text(
                        text = it.name,
                        fontStyle = FontStyle.Italic,
                    )
                    Spacer(Modifier.width(16.dp))
                }

            }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (dirty.value) {
                    val newUser = userRepository.updateUser(user.value, currentUser.asPrincipal())
                    serviceViewModel.user.value = newUser
                    navController.navigate(Screen.List)
                }
            },
            enabled = passwordConfirmation.value.text == user.value.password,
            modifier = Modifier.align(Alignment.CenterHorizontally).testTag("submit")
        ) {
            Text("Submit")

        }
    }
}