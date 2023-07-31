package passwordStore

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.kodein.di.compose.rememberInstance
import passwordStore.navigation.NavController
import passwordStore.services.ServiceViewModel
import passwordStore.users.UpdateUser
import passwordStore.users.User
import passwordStore.users.UserRepository

@Composable
fun settings(currentUser: User) {

    val user = remember {
        mutableStateOf(
            UpdateUser(
                userid = currentUser.userid,
                email = currentUser.email,
                fullName = currentUser.fullName,
                password = ""
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
            visualTransformation = PasswordVisualTransformation(),
            onValueChange = { password ->
                user.value = user.value.copy(password = password)
                dirty.value = true
            },
            isError = passwordConfirmation.value.text != user.value.password,
            modifier = Modifier.align(Alignment.CenterHorizontally).testTag("password")
        )
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