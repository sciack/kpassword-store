package passwordStore.config

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
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

@Composable
fun ConfigView() {
    val configVM by rememberInstance<ConfigVM>()
    val coroutineScope = rememberCoroutineScope()
    val jdbc = remember {
        mutableStateOf(TextFieldValue(configVM.jdbcUrl))
    }
    val secret = remember {
        mutableStateOf(TextFieldValue(configVM.secret))
    }
    val dbPassword = remember {
        mutableStateOf(TextFieldValue(configVM.dbPassword))
    }
    val navController by rememberInstance<NavController>()
    Column(modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = jdbc.value,
                onValueChange = { value: TextFieldValue ->
                    jdbc.value = value
                },
                label = { Text("Jdbc Url") }
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = dbPassword.value,
                onValueChange = { value: TextFieldValue ->
                    dbPassword.value = value
                },
                label = { Text("Database Password") }
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = secret.value,
                onValueChange = { value: TextFieldValue ->
                    secret.value = value
                },
                label = { Text("Secret") }
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
        }


    }
}