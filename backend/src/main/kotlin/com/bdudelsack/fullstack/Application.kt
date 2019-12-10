package com.bdudelsack.fullstack

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.html.respondHtml
import io.ktor.http.content.resource
import io.ktor.http.content.static
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.DefaultJsonConfiguration
import io.ktor.serialization.serialization
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.html.*
import kotlinx.serialization.json.Json

val todos = listOf(
    TodoItem("0", "This is a first todo item"),
    TodoItem("1", "This is a second todo item")
)

fun main() {
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        install(ContentNegotiation) {
            serialization(
                json = Json(
                    DefaultJsonConfiguration.copy(
                        prettyPrint = true
                    )
                )
            )
        }
        routing {
            get("/") {
                call.respondHtml {
                    head {
                        title("Hello from Kotlin Fullstack example!")

                    }

                    body {
                        div {
                            id = "container"
                        }

                        script {
                            src = "/static/frontend.js"
                        }
                    }
                }
            }

            get("/todos") {
                call.respond(todos)
            }

            static("/static") {
                resource("frontend.js")
            }
        }
    }.start(wait = true)
}
