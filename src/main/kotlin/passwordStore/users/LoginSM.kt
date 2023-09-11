package passwordStore.users

import androidx.compose.ui.text.input.TextFieldValue
import cafe.adriel.voyager.core.model.ScreenModel
import passwordStore.LOGGER
import passwordStore.utils.obfuscate


class LoginSM(private val userRepository: UserRepository) : ScreenModel {

    fun login(username: TextFieldValue, password: TextFieldValue): Result<User> {
        LOGGER.info {
            """Username: ${username.text}
        |Password: ${password.text.obfuscate()}
    """.trimMargin()
        }

        return runCatching {
            userRepository.login(username.text, password.text)
        }.onFailure {
            LOGGER.warn(it) { "Wrong credentials" }
        }
    }
}