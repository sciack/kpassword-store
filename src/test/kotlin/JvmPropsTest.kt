import kotlin.test.Test

class JvmPropsTest {

    @Test
    fun `show properties`() {
        System.getProperties().toSortedMap { first: Any, second: Any ->
            first.toString().compareTo(second.toString())
        }.forEach { (key, value) ->
            println("$key: $value")
        }
    }
}