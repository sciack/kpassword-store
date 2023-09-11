package passwordStore.users

import com.github.javafaker.Faker
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isEmptyString
import kotlinx.coroutines.test.runTest
import org.awaitility.kotlin.await
import org.kodein.di.instance
import passwordStore.DiInjection
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class CreateUserSMTest {

    private val di = DiInjection.testDi
    private val createUserSM by di.instance<CreateUserSM>()
    private val userRepository by di.instance<UserRepository>()

    @Test
    fun `should create a user`() = runTest {
        val user = createUser("testInsert")
        assertThat(user.roles, equalTo(setOf(Roles.NormalUser)))
        assertThat(user.fullName, !isEmptyString)
        assertThat(user.email, !isEmptyString)
        assertThat(createUserSM.errorMsg.value, isEmptyString)
        userRepository.deleteUser(user.userid)
    }

    @Test
    fun `should create a fail to create a user if duplicate`() = runTest {
        val user = createUser("testInsert")
        createUser("testInsert")
        await.atMost(2.seconds.toJavaDuration()).untilAsserted {
            assertThat(createUserSM.errorMsg.value, !isEmptyString)
        }
        userRepository.deleteUser(user.userid)
    }


    private suspend fun createUser(userId: String): User {
        val faker = Faker()
        val user = EditableUser(
            userid = userId,
            fullName = faker.friends().character(),
            email = faker.internet().emailAddress(),
            password = faker.internet().password(),
            roles = setOf(Roles.NormalUser)
        )
        createUserSM.createUser(user)
        await.atMost(2.seconds.toJavaDuration()).until {
            runCatching {
                userRepository.findUser(userid = user.userid)
            }.isSuccess
        }
        return userRepository.findUser(userid = user.userid)
    }
}