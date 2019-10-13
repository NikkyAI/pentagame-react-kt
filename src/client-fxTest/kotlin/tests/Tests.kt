import kotlinx.serialization.list
import mu.KotlinLogging
import penta.SerialNotation
import penta.json
import java.io.File
import kotlin.test.*

object Tests {
    private val logger = KotlinLogging.logger {}
    @Test
    fun replay() {
        logger.info { "test" }
        val testFile = File(System.getProperty("user.home")).resolve("dev/pentagame/src/client-fxTest/resources/test2.json")
        val testJson = testFile.readText()
        val notationList = json.parse(SerialNotation.serializer().list, testJson)

        notationList.forEach {
            logger.info { it }
        }

        val testState = TestState()
        val moves = SerialNotation.toMoves(notationList, testState) {
            testState.processMove(it)
        }

        moves.forEach {
            logger.info { it }
        }
    }
}

private val logger = KotlinLogging.logger {}
fun String.asResource(work: (String) -> Unit) {
    logger.info{javaClass}
    val content = this.javaClass.getResource(this).readText()
    work(content)
}

