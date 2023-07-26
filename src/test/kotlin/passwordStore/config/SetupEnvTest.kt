package passwordStore.config

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlin.test.Test

class SetupEnvTest {


    @Test
    fun `should setup environment file`() {
        SetupEnv.configure("test.env")
        assertThat(System.getProperty("secret"), equalTo("MySecret"))
    }

}