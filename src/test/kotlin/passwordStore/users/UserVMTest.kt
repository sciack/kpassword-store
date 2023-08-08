package passwordStore.users

import com.github.javafaker.Faker
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isEmptyString
import kotlinx.coroutines.test.runTest
import org.awaitility.kotlin.await
import org.kodein.di.instance
import passwordStore.DiInjection
import passwordStore.services.Service
import passwordStore.services.ServicesRepository
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration


class UserVMTest {

    private val di = DiInjection.testDi
    private val userVM by di.instance<UserVM>()
    private val userRepository by di.instance<UserRepository>()

    @Test
    fun loadUser() = runTest {
        userVM.loadUsers()
        val users = userVM.users
        await.atMost(2.seconds.toJavaDuration()).untilAsserted {
            assertThat(users.size, equalTo(3))
        }
    }

    @Test
    fun `should delete a user`() = runTest {
        val user = createUser("testDelete")
        userVM.delete(user.userid)
        await.atMost(2.seconds.toJavaDuration()).until {
            userRepository.list().none { it.userid == user.userid }
        }
    }

    @Test
    fun `should fail to delete a user if has services`() = runTest {
        val user = userRepository.findUser("dummy")
        val serviceRepository by di.instance<ServicesRepository>()
        val faker = Faker()
        serviceRepository.store(
            Service(
                service = "testService",
                username = faker.friends().character(),
                password = faker.internet().password(),
                userid = user.userid,
                dirty = true
            )
        )
        userVM.delete(userid = user.userid)
        await.atMost(2.seconds.toJavaDuration()).untilAsserted {
            assertThat(
                userVM.errorMsg.value,
                equalTo("The user has service stored, before delete the user all the service must be deleted")
            )
        }

    }

    @Test
    fun `should create a user`() = runTest {
        val user = createUser("testInsert")
        assertThat(user.roles, equalTo(setOf(Roles.NormalUser)))
        assertThat(user.fullName, !isEmptyString)
        assertThat(user.email, !isEmptyString)
        assertThat(userVM.errorMsg.value, isEmptyString)
        userRepository.deleteUser(user.userid)
    }

    @Test
    fun `should create a fail to create a user if duplicate`() = runTest {
        val user = createUser("testInsert")
        createUser("testInsert")
        await.atMost(2.seconds.toJavaDuration()).untilAsserted {
            assertThat(userVM.errorMsg.value, !isEmptyString)
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
        userVM.createUser(user)
        await.atMost(2.seconds.toJavaDuration()).until {
            runCatching {
                userRepository.findUser(userid = user.userid)
            }.isSuccess
        }
        return userRepository.findUser(userid = user.userid)
    }
}