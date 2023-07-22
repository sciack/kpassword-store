package passwordStore

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import passwordStore.users.User

@Composable
fun servicesTable(services: List<Service>) {
    val rows = services.map { row ->
        Row(Modifier.padding(16.dp)) {
            Text(row.service)
            Text(row.userid)
            Text(row.username)
            Text(row.password)
        }
    }
    Column(Modifier.padding(16.dp)) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.Center) {
            header.forEach { text ->
                Text(text)
            }
        }
        rows
    }
}

private val header = arrayOf<String>("Col 1", "Col2")


@Composable
fun ServiceDetailsScreen(item: Service, onBackClick: () -> Unit) {
    Column {
        TopAppBar(
            title = { Text("Item details") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        )

        Text(text = item.service)
    }
}

@Composable
fun newService(user: User, onSubmit: (Service) -> Unit) {
    val service = remember {
        mutableStateOf(TextFieldValue())
    }
    val username = remember {
        mutableStateOf(TextFieldValue())
    }
    val password = remember {
        mutableStateOf(TextFieldValue())
    }
    val note = remember {
        mutableStateOf(TextFieldValue())
    }

    Column {
        TopAppBar(
            title = { Text("New Service") }
        )
        Row {
            Text("Service:")
            Spacer(Modifier.width(16.dp))
            OutlinedTextField(
                label = { Text("Service") },
                onValueChange = { service.value = it },
                value = service.value
            )
        }
        Row {
            Text("Username:")
            Spacer(Modifier.width(16.dp))
            OutlinedTextField(
                label = { Text("Username") },
                value = username.value,
                onValueChange = { username.value =  it }
            )
        }
        Row {
            Text("Password:")
            Spacer(Modifier.width(16.dp))
            OutlinedTextField(
                label = { Text("Password") },
                value = password.value,
                onValueChange = { password.value =  it }
            )
        }

        Row {
            Text("Note:")
            Spacer(Modifier.width(16.dp))
            OutlinedTextField(
                label = { Text("Note") },
                value = note.value,
                minLines = 5,
                maxLines = 10,
                onValueChange = { note.value =  it }
            )
        }
        Row {
            Button(
                onClick = {
                    val service = Service(
                        service= service.value.text,
                        username= username.value.text,
                        password = password.value.text,
                        note = note.value.text,
                        userid = user.userid
                    )
                    onSubmit(service)
                }
            ) {
                Text("Submit")
            }
        }
    }
}