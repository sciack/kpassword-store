package passwordStore.users

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import passwordStore.ui.theme.*
import passwordStore.utils.LocalStatusHolder
import passwordStore.widget.EditorCard
import passwordStore.widget.Table
import passwordStore.widget.passwordDialog
import passwordStore.widget.showOkCancel

@Composable
fun userSettings(userVM: UserVM) {
    val currentUser = LocalUser.currentOrThrow
    val navController = LocalNavigator.currentOrThrow
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

    editUser(userVM, user) {
        navController.pop()
    }

}

@Composable
fun editUser(userVM: UserVM, user: MutableState<EditableUser>, back: () -> Unit) {
    val dirty = remember {
        mutableStateOf(false)
    }

    val passwordConfirmation = remember {
        mutableStateOf(TextFieldValue())
    }

    val currentUser = LocalUser.currentOrThrow
    val setUser = LocalSetUser.current

    val coroutineScope = rememberCoroutineScope()
    val errorMsg = remember {
        userVM.errorMsg
    }

    Column(Modifier.padding(XXL)) {
        userFields(user, dirty, passwordConfirmation)
        if (errorMsg.value.isNotEmpty()) {
            Text(
                errorMsg.value,
                modifier = Modifier.align(Alignment.CenterHorizontally).testTag("New User errorMsg"),
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(0.8f, TextUnitType.Em)
            )
        }
        Spacer(Modifier.height(LARGE))
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Button(
                onClick = {
                    if (dirty.value) {
                        coroutineScope.launch {
                            userVM.updateUser(user.value).onSuccess { newUser ->
                                if (currentUser.userid == newUser.userid) {
                                    setUser(newUser)
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
            Spacer(Modifier.width(XL))
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
    passwordConfirmation: MutableState<TextFieldValue>

) {
    val currentUser = LocalUser.currentOrThrow
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
    Spacer(Modifier.height(LARGE))
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
    Spacer(Modifier.height(LARGE))
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
    Spacer(Modifier.height(LARGE))
    Card(Modifier.Companion.align(Alignment.CenterHorizontally).padding(XS)) {
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
    Spacer(Modifier.height(LARGE))
    if (currentUser.isAdmin()) {
        Row(modifier = Modifier.Companion.align(Alignment.CenterHorizontally)) {
            Column {
                Text("Roles:")

                Roles.entries.forEach { role ->
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
                        Spacer(Modifier.width(LARGE))
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
                Spacer(Modifier.width(LARGE))
            }

        }
    }
}

@Composable
fun createUser(createUserSM: CreateUserSM) {

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

    val navController = LocalNavigator.currentOrThrow
    val coroutineScope = rememberCoroutineScope()
    val errorMsg = remember {
        createUserSM.errorMsg
    }

    Column(Modifier.padding(XXL)) {
        OutlinedTextField(
            value = user.value.userid,
            onValueChange = { value ->
                user.value = user.value.copy(userid = value)
                dirty.value = true
            },
            singleLine = true
        )
        Spacer(Modifier.height(LARGE))
        userFields(user, dirty, passwordConfirmation)
        Spacer(Modifier.height(LARGE))
        if (errorMsg.value.isNotEmpty()) {
            Text(
                errorMsg.value,
                modifier = Modifier.align(Alignment.CenterHorizontally).testTag("New User errorMsg"),
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(0.8f, TextUnitType.Em)
            )
        }
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Button(
                onClick = {
                    if (dirty.value) {
                        coroutineScope.launch {
                            createUserSM.createUser(user.value).onSuccess {
                                navController.pop()
                            }
                        }
                    }
                },
                enabled = passwordConfirmation.value.text == user.value.password,
                modifier = Modifier.testTag("submit")
            ) {
                Text("Submit")

            }
            Spacer(Modifier.width(XL))
            Button(
                onClick = {
                    errorMsg.value = ""
                    navController.pop()
                },
                modifier = Modifier.testTag("cancel")
            ) {
                Text("Cancel")

            }
        }
    }
}

@Composable
fun users(userVM: UserVM) {

    val coroutineScope = rememberCoroutineScope()
    val currentUser = LocalUser.currentOrThrow

    val users = remember {
        userVM.users
    }
    val selectedUser = remember {
        mutableStateOf(EditableUser())
    }
    val statusHolder = LocalStatusHolder.currentOrThrow

    Column(modifier = Modifier.fillMaxWidth(0.9f).padding(LARGE)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Table(
                modifier = Modifier.fillMaxSize(),
                headers = listOf("Username", "Full Name", "Email", "Roles", "Services"),
                values = users.toList(),
                cellContent = { columnIndex, user ->
                    cell(user, columnIndex)
                },
                beforeRow = { user ->
                    if (currentUser.isAdmin()) {
                        Row {
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
                                    Icons.Default.Person,
                                    "Edit",
                                )
                            }
                            Spacer(Modifier.width(SMALL))
                            val showAlert = remember {
                                mutableStateOf(false)
                            }
                            IconButton(
                                onClick = { showAlert.value = true },
                                modifier = Modifier.testTag("Delete ${user.userid}")
                            ) {
                                Icon(
                                    Icons.Default.PersonRemove,
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
                                            statusHolder.sendMessage("Error delete user ${user.userid}: ${it.localizedMessage}")
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
        EditorCard(onCloseRequest = {
            selectedUser.value = EditableUser()
        }) {

            editUser(userVM, selectedUser) {
                coroutineScope.launch {
                    userVM.loadUsers()
                }
                close()
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