package penta.server

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import io.ktor.websocket.webSocket
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.delay
import kotlinx.serialization.list
import kotlinx.serialization.serializer
import mu.KotlinLogging
import penta.SerialNotation
import penta.json
import penta.network.GameSessionInfo
import penta.network.LoginRequest
import penta.network.LoginResponse
import penta.network.ServerStatus
import penta.util.suspendDebug
import penta.util.suspendError

private val logger = KotlinLogging.logger {}
fun Application.routes() = routing {
    val received = mutableListOf<String>()
    webSocket("/") {
        // websocketSession
        while (true) {
            when (val frame = incoming.receive()) {
                is Frame.Text -> {
                    val text = frame.readText()
                    outgoing.send(Frame.Text("YOU SAID: $text"))
                    if (text.equals("bye", ignoreCase = true)) {
                        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                    }
                }
            }
        }
    }
    webSocket("/echo") {
        logger.debug { "onConnect" }
        try {
            while (true) {
                val text = (incoming.receive() as Frame.Text).readText()
                logger.info { "onMessage $text" }
                received += text
                outgoing.send(Frame.Text(text))
            }
        } catch (e: ClosedReceiveChannelException) {
            logger.suspendDebug(e) { "onClose ${closeReason.await()}" }
        } catch (e: Throwable) {
            logger.suspendError(e) { "onClose ${closeReason.await()}" }
        }
    }
    webSocket("/replay") {
        logger.info { "onConnect" }

        val gameJson = (incoming.receive() as Frame.Text).readText()

        val notationList = json.parse(SerialNotation.serializer().list, gameJson)


        notationList.forEach {
            delay(500)
            val notationJson = json.stringify(SerialNotation.serializer(), it)
            logger.info { "sending: $notationJson" }
            outgoing.send(Frame.Text(notationJson))
        }
        logger.info { "done" }

        close(CloseReason(CloseReason.Codes.NORMAL, "Replay done"))
    }
    get("/api/status") {
        logger.info { "received status request" }
        call.respondText(
            text = json.stringify(
                ServerStatus.serializer(),
                ServerStatus(
                    totalPlayers = 0
                )
            ),
            contentType = ContentType.Application.Json
        )
    }
    get("/api/user/{userid}") {
        val userid = call.parameters["userid"]
        // TODO retreive public user data
        call.respondText(
            contentType = ContentType.Application.Json,
            status = HttpStatusCode.NotFound,
            text = "No such user by id '$userid'"
        )
    }
    post("/api/login") {
        val loginRequest = call.receive<LoginRequest>()
        // find registered user
        val user: String? = listOf("alice", "bob").find { it == loginRequest.userId }
        val response: LoginResponse = if (user == null) {
            when {
                loginRequest.userId.length < 5 ->
                    LoginResponse.UserIdRejected(
                        reason = "userId is too short"
                    )

                loginRequest.userId.length > 16 ->
                    LoginResponse.UserIdRejected(
                        reason = "userId is too long"
                    )

                else -> {
                    val illegalMatches = "[^A-Za-z0-9_-]".toRegex().findAll(loginRequest.userId).toList()
                    if (illegalMatches.isNotEmpty()) {
                        val illegalChars = illegalMatches.map {
                            it.value
                        }.toSet().joinToString(" ")
                        LoginResponse.UserIdRejected(
                            reason = "userId contains illegal characters: $illegalChars"
                        )
                    } else {
                        // create temporary user
                        val tmpUser = User.TemporaryUser(loginRequest.userId)
                        val tmpSession = UserSession(tmpUser.userId)
                        call.sessions.set(tmpSession)

                        LoginResponse.Success(
                            message = "Welcome ${tmpUser.displayName}"
                        )
                    }
                }
            }

            // create temporary session
        } else {
            // TODO: retrieve User
            val registeredUser = User.RegisteredUser(loginRequest.userId, passwordHash = "password")
            if (loginRequest.password != registeredUser.passwordHash) {
                LoginResponse.IncorrectPassword
            } else {
                val authenticatedSession = UserSession(registeredUser.userId)
                call.sessions.set(authenticatedSession)

                LoginResponse.Success(
                    message = "Welcome back ${registeredUser.displayName}"
                )
            }
        }

        call.respondText(
            contentType = ContentType.Application.Json,
            text = json.stringify(
                LoginResponse.serializer(),
                response
            ),
            status = HttpStatusCode.OK
        )
    }

    get("/api/games") {
        val session = call.sessions.get<UserSession>()

        if (session == null) {
            call.respondText(
                text = "not logged in",
                status = HttpStatusCode.Unauthorized
            )
        } else {
            call.respondText(
                text = json.stringify(
                    GameSessionInfo.serializer().list,
                    GameController.games.map { gameState ->
                        gameState.info
                    }
                ),
                contentType = ContentType.Application.Json
            )
        }
    }

    get("/whoami") {
        val session = call.sessions.get<UserSession>()
        call.respondText(
            session.toString()
        )
    }
    webSocket("/api/game/{gameId}") {
        val gameId = call.parameters["gameId"] ?: throw IllegalArgumentException("missing parameter gameId")

        val session = call.sessions.get<UserSession>() ?: run {
            return@webSocket close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "not authenticated"))
        }

        val game = GameController.games.find {
            it.id == gameId
        } ?: run {
            return@webSocket close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "game not found"))
        }

        game.handle(this, session)
    }
}
