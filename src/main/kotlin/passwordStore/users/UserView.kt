package passwordStore.users

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance
import passwordStore.navigation.NavController
import passwordStore.services.ServiceVM
import passwordStore.utils.StatusHolder
import passwordStore.widget.Table
import passwordStore.widget.bottomBorder
import passwordStore.widget.passwordDialog
import passwordStore.widget.showOkCancel

@Composable
fun userSettings(currentUser: User) {
    val navController by rememberInstance<NavController>()
    val user = remember {
        mutableStateOf(
            EditableUser(
                userid = currentUser.userid,
                email = currentUser.email,
                fullName = currentUser.fullName,
                password = "",
                roles = currentUser.roles
            )
        )
    }

    editUser(user) {
        navController.navigateBack()
    }

}

@Composable
fun editUser(user: MutableState<EditableUser>, back: () -> Unit) {
    val dirty = remember {
        mutableStateOf(false)
    }

    val passwordConfirmation = remember {
        mutableStateOf(TextFieldValue())
    }

    val userVM by rememberInstance<UserVM>()
    val serviceVM by rememberInstance<ServiceVM>()

    val coroutineScope = rememberCoroutineScope()
    val errorMsg = remember {
        userVM.errorMsg
    }

    Column(Modifier.padding(32.dp)) {
        userFields(user, dirty, passwordConfirmation, serviceVM)
        if (errorMsg.value.isNotEmpty()) {
            Text(
                errorMsg.value,
                modifier = Modifier.align(Alignment.CenterHorizontally).testTag("New User errorMsg"),
                color = MaterialTheme.colors.error,
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(0.8f, TextUnitType.Em)
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Button(
                onClick = {
                    if (dirty.value) {
                        coroutineScope.launch {
                            userVM.updateUser(user.value, serviceVM.user.value.asPrincipal()).onSuccess { newUser ->
                                if (serviceVM.user.value.userid == newUser.userid) {
                                    serviceVM.user.value = newUser
                                }
                                back()
                            }
                        }
                    }
                },
                enabled = passwordConfirmation.value.text == user.value.password,
                modifier = Modifier.testTag("submit")
            ) {
                Text("Submit")

            }
            Spacer(Modifier.width(24.dp))
            Button(
                onClick = {
                    errorMsg.value = ""
                    back()
                },
                modifier = Modifier.testTag("cancel")
            ) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun ColumnScope.userFields(
    user: MutableState<EditableUser>,
    dirty: MutableState<Boolean>,
    passwordConfirmation: MutableState<TextFieldValue>,
    serviceVM: ServiceVM,
) {
    val showPasswordDialog = remember {
        mutableStateOf(false)
    }
    OutlinedTextField(
        value = user.value.fullName,
        onValueChange = { name ->
            user.value = user.value.copy(fullName = name)
            dirty.value = true
        },
        label = {
            Text("Fullname")
        },
        singleLine = true,
        modifier = Modifier.Companion.align(Alignment.CenterHorizontally).testTag("fullName")
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
        singleLine = true,
        modifier = Modifier.Companion.align(Alignment.CenterHorizontally).testTag("email")
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
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        onValueChange = { password ->
            user.value = user.value.copy(password = password)
            dirty.value = true
        },
        isError = passwordConfirmation.value.text != user.value.password,
        modifier = Modifier.Companion.align(Alignment.CenterHorizontally).testTag("password")
    )
    Spacer(Modifier.height(12.dp))
    Card(Modifier.Companion.align(Alignment.CenterHorizontally).padding(4.dp)) {
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
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        onValueChange = { password ->
            passwordConfirmation.value = password
            dirty.value = true
        },
        isError = passwordConfirmation.value.text != user.value.password,
        modifier = Modifier.Companion.align(Alignment.CenterHorizontally).testTag("password-confirmation")
    )
    Spacer(Modifier.height(16.dp))
    if (serviceVM.user.value.isAdmin()) {
        Row(modifier = Modifier.Companion.align(Alignment.CenterHorizontally)) {
            Column {
                Text("Roles:")

                Roles.values().forEach { role ->
                    Row {
                        Checkbox(checked = user.value.roles.contains(role),
                            onCheckedChange = { value ->
                                val roles = user.value.roles.toMutableSet()
                                if (value) {
                                    roles.add(role)
                                } else {
                                    roles.remove(role)
                                }
                                dirty.value = true
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
        Row(modifier = Modifier.Companion.align(Alignment.CenterHorizontally)) {
            Text("Roles:")
            user.value.roles.forEach {
                Text(
                    text = it.name,
                    fontStyle = FontStyle.Italic,
                )
                Spacer(Modifier.width(16.dp))
            }

        }
    }
}

@Composable
fun createUser() {
    val user = remember {
        mutableStateOf(
            EditableUser()
        )
    }

    val dirty = remember {
        mutableStateOf(false)
    }

    val passwordConfirmation = remember {
        mutableStateOf(TextFieldValue())
    }

    val userVM by rememberInstance<UserVM>()
    val serviceVM by rememberInstance<ServiceVM>()
    val navController by rememberInstance<NavController>()
    val coroutineScope = rememberCoroutineScope()
    val errorMsg = remember {
        userVM.errorMsg
    }

    Column(Modifier.padding(32.dp)) {
        OutlinedTextField(
            value = user.value.userid,
            onValueChange = { value ->
                user.value = user.value.copy(userid = value)
                dirty.value = true
            },
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
        userFields(user, dirty, passwordConfirmation, serviceVM)
        Spacer(Modifier.height(16.dp))
        if (errorMsg.value.isNotEmpty()) {
            Text(
                errorMsg.value,
                modifier = Modifier.align(Alignment.CenterHorizontally).testTag("New User errorMsg"),
                color = MaterialTheme.colors.error,
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(0.8f, TextUnitType.Em)
            )
        }
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Button(
                onClick = {
                    if (dirty.value) {
                        coroutineScope.launch {
                            userVM.createUser(user.value).onSuccess {
                                navController.navigateBack()
                            }
                        }
                    }
                },
                enabled = passwordConfirmation.value.text == user.value.password,
                modifier = Modifier.testTag("submit")
            ) {
                Text("Submit")

            }
            Spacer(Modifier.width(24.dp))
            Button(
                onClick = {
                    errorMsg.value = ""
                    navController.navigateBack()
                },
                modifier = Modifier.testTag("cancel")
            ) {
                Text("Cancel")

            }
        }
    }
}

@Composable
fun users() {
    val userVM by rememberInstance<UserVM>()
    val serviceVM by rememberInstance<ServiceVM>()
    val navController by rememberInstance<NavController>()
    val coroutineScope = rememberCoroutineScope()
    val users = remember {
        userVM.users
    }
    val selectedUser = remember {
        mutableStateOf(EditableUser())
    }

    Column(modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Table(
                modifier = Modifier.fillMaxSize(),
                rowModifier = Modifier.fillMaxWidth().bottomBorder(1.dp, color = Color.LightGray),
                rowCount = users.size,
                headers = listOf("Username", "Full Name", "Email", "Roles", "Services"),
                values = users.toList(),
                cellContent = { columnIndex, user ->
                    cell(user, columnIndex)
                },
                beforeRow = { user ->
                    if (serviceVM.user.value.isAdmin()) {
                        Row(modifier = Modifier.align(Alignment.CenterVertically)) {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        selectedUser.value = EditableUser(
                                            user.userid, user.fullName, user.email, "", user.roles
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("Edit ${user.userid}")
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    "Edit",

                                    )
                            }
                            val showAlert = remember {
                                mutableStateOf(false)
                            }
                            IconButton(
                                onClick = { showAlert.value = true },
                                modifier = Modifier.testTag("Delete ${user.userid}")
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    "Delete",
                                    )
                            }

                            showOkCancel(
                                title = "Delete confirmation",
                                message = "Do you want to delete user ${user.userid}?",
                                showAlert,
                                onConfirm = {
                                    coroutineScope.launch {
                                        userVM.delete(user.userid).onFailure {
                                            StatusHolder.sendMessage("Error delete user ${user.userid}: ${it.localizedMessage}")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            )
        }
    }

    if (selectedUser.value.userid.isNotEmpty()) {
        Card(modifier = Modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            val maxWidth = constraints.maxWidth
            val x = (maxWidth - placeable.width).coerceAtLeast(0)
            layout(width = placeable.width, height = placeable.height) {
                placeable.place(x, 10)
            }
        }) {

            editUser(selectedUser) {
                coroutineScope.launch {
                    userVM.loadUsers()
                }
                selectedUser.value = EditableUser()

            }
        }
    }
}

@Composable
fun cell(user: ListUser, col: Int) {
    when (col) {
        0 -> Text(user.userid)
        1 -> Text(user.fullName)
        2 -> Text(user.email)
        3 -> Text(user.roles.joinToString(","))
        4 -> Text(user.services.toString())
        else -> Text("")
    }

}