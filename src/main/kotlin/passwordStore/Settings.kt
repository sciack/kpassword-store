package passwordStore

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import passwordStore.users.UpdateUser
import passwordStore.users.User

@Composable
fun settings(currentUser: User) {

    val user = remember {
        mutableStateOf(UpdateUser(
            userid = currentUser.userid,
            email = currentUser.email,
            fullName = currentUser.fullName,
            password = ""
        ))
    }

    Column {
        OutlinedTextField(
            value = user.value.fullName,
            onValueChange = {name ->
                user.value = user.value.copy(fullName = name)
            },
            label = {
                Text("Fullname")
            }

        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            label = {
                Text("email")
            },
            value = user.value.email,
            onValueChange = { email ->
                user.value = user.value.copy(email = email)
            }
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            label = {
                Text("password")
            },
            value = user.value.password,
            visualTransformation = PasswordVisualTransformation(),
            onValueChange = { password ->
                user.value = user.value.copy(password = password)
            }
        )
        Spacer(Modifier.height(16.dp))
        Row {
            currentUser.roles.forEach{
                Text(it.name)
                Spacer(Modifier.width(16.dp))
            }

        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = {},
            ) {
            Text("Submit")
        }
    }
}