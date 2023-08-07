package passwordStore.config

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.Test

class SetupEnvTest {


    @Test
    fun `should setup environment file`() {
        SetupEnv.configure(Path.of("test.env"))
        assertThat(System.getProperty("secret"), equalTo("MySecret"))
    }

}