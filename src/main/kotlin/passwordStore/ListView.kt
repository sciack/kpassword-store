package passwordStore

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import passwordStore.users.User

@Composable
fun servicesTable(services: List<Service>) {
    serviceList(services)
}

@Composable
private fun serviceList(services: List<Service>) {
    Box(
        modifier = Modifier.fillMaxSize()
            .padding(10.dp)
    ) {
        val state = rememberLazyListState()

        LazyColumn(Modifier.fillMaxSize().padding(end = 12.dp), state) {
            items(services) { service ->
                Text(service.service)
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(
                scrollState = state
            )
        )
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
            onValueChange = { username.value = it }
        )
    }
    Row {
        Text("Password:")
        Spacer(Modifier.width(16.dp))
        OutlinedTextField(
            label = { Text("Password") },
            value = password.value,
            onValueChange = { password.value = it }
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
            onValueChange = { note.value = it }
        )
    }
    Row {
        Button(
            onClick = {
                val service = Service(
                    service = service.value.text,
                    username = username.value.text,
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