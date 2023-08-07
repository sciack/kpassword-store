package passwordStore.users

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.test.runTest
import org.awaitility.kotlin.await
import org.kodein.di.instance
import passwordStore.DiInjection
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration


class UserVMTest {

    private val di = DiInjection.testDi
    private val userVM by di.instance<UserVM>()


    @Test
    fun loadUser() = runTest {
        userVM.loadUsers()
        val users = userVM.users
        await.atMost(2.seconds.toJavaDuration()).untilAsserted {
            assertThat(users.size, equalTo(3))
        }
    }
}