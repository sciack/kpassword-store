package passwordStore.config

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.compose.rememberInstance
import passwordStore.navigation.NavController
import passwordStore.users.UserVM

@Composable
fun configView() {
    val configVM by rememberInstance<ConfigVM>()
    val coroutineScope = rememberCoroutineScope()
    val userVM by rememberInstance<UserVM>()
    val jdbc = remember {
        mutableStateOf(TextFieldValue(configVM.jdbcUrl))
    }
    val secret = remember {
        mutableStateOf(TextFieldValue(configVM.secret))
    }
    val dbPassword = remember {
        mutableStateOf(TextFieldValue(configVM.dbPassword))
    }
    val ivSpec = remember {
        mutableStateOf(TextFieldValue(configVM.ivSpec))
    }
    val darkMode = remember {
        configVM.darkMode
    }
    val navController by rememberInstance<NavController>()
    Column(modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp)) {
        Row {
            Text(
                "Dark mode",
                Modifier.padding(end = 16.dp)
            )
            RadioButton(darkMode.value == DARK_MODES.LIGHT,
                onClick = {
                    configVM.darkMode.value = DARK_MODES.LIGHT
                    darkMode.value = DARK_MODES.LIGHT
                })
            Text("Light")
            Spacer(Modifier.width(16.dp))
            RadioButton(darkMode.value == DARK_MODES.DARK,
                onClick = {
                    configVM.darkMode.value = DARK_MODES.DARK
                    darkMode.value = DARK_MODES.DARK
                })
            Text("Dark")
            Spacer(Modifier.width(16.dp))
            RadioButton(darkMode.value == DARK_MODES.SYSTEM_DEFAULT,
                onClick = {
                    configVM.darkMode.value = DARK_MODES.SYSTEM_DEFAULT
                    darkMode.value = DARK_MODES.SYSTEM_DEFAULT
                })
            Text("System Default")
        }
        Divider()
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = jdbc.value,
                onValueChange = { value: TextFieldValue ->
                    configVM.jdbcUrl = value.text
                    jdbc.value = value
                },
                label = { Text("Jdbc Url") },
                readOnly = !userVM.isAdmin()
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = dbPassword.value,
                onValueChange = { value: TextFieldValue ->
                    configVM.dbPassword = value.text
                    dbPassword.value = value
                },
                label = { Text("Database Password") },
                readOnly = !userVM.isAdmin()
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = secret.value,
                onValueChange = { value: TextFieldValue ->
                    configVM.secret = value.text
                    secret.value = value
                },
                label = { Text("Secret") },
                readOnly = !userVM.isAdmin()
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Changing the secret could make the database not usable anymore and require a reload of all the data, should be done with an empty database",
                fontWeight = FontWeight.Light,
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.body2
            )
        }
        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = ivSpec.value,
                onValueChange = { value: TextFieldValue ->
                    val text = if (value.text.length > 16) {
                        value.text.substring(0 until 16)
                    } else {
                        value.text
                    }
                    configVM.ivSpec = text
                    ivSpec.value = value.copy(text)
                },
                isError = ivSpec.value.text.toByteArray().size != 16,
                label = { Text("IV spec string") },
                readOnly = !userVM.isAdmin()
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Must be 16 byte string",
                fontWeight = FontWeight.Light,
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.body2
            )
        }
        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        configVM.save()
                        withContext(Dispatchers.Main) {
                            navController.navigateBack()
                        }
                    }
                }
            ) {
                Text("Save")
            }
            Spacer(Modifier.width(16.dp))
            Button(
                onClick = {
                    configVM.reset()
                    navController.navigateBack()
                }
            ) {
                Text("Cancel")
            }
        }


    }
}