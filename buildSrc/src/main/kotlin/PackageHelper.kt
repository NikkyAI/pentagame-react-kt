import java.io.File
import kotlinx.html.stream.appendHTML
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.script
import java.io.FileFilter

fun packageJs(jsInput: File, htmlOutput: File) {
    htmlOutput.deleteRecursively()
    htmlOutput.mkdir()
    val jsOutput = htmlOutput.resolve("js-min").apply {
        mkdir()
    }

    jsInput.listFiles().forEach { file ->
        val target = jsOutput.resolve(file.name)
        target.createNewFile()
        file.copyTo(target)
    }

    val htmlText = buildString {
        appendln("<!DOCTYPE html>")
        appendHTML().html {
            body {
                a("http://kotlinlang.org") { +"link" }
                jsOutput.listFiles(FileFilter { file -> file.name.endsWith(".js") }).map {
                    it.relativeTo(htmlOutput).path.replace("\\", "/")
                }.forEach {
                    script(type = "text/javascript", src = it) {}
                }
            }

        }
        appendln()
    }
    htmlOutput.resolve("index.html").writeText(htmlText)
}