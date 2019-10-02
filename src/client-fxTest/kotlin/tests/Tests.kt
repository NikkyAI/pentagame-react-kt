import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import kotlinx.serialization.modules.SerializersModule
import penta.SerialNotation
import java.io.File
import kotlin.test.*

val json = Json(JsonConfiguration(unquoted = false, allowStructuredMapKeys = true, prettyPrint = true, classDiscriminator = "type"), context = SerializersModule {
    SerialNotation.install(this)
})

object Tests {
    @Test
    fun replay() {
        println("test")
        val testFile = File(System.getProperty("user.home")).resolve("dev/pentagame/src/client-fxTest/resources/test2.json")
        val testJson = testFile.readText()
        val notationList = json.parse(SerialNotation.serializer().list, testJson)

        notationList.forEach {
            println(it)
        }

        val testState = TestState()
        val moves = SerialNotation.toMoves(notationList, testState) {
            testState.processMove(it)
        }

        moves.forEach(::println)
    }
}

fun String.asResource(work: (String) -> Unit) {
    println("${this.javaClass}")
    val content = this.javaClass.getResource(this).readText()
    work(content)
}

