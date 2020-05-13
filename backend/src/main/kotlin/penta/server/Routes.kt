package penta.server

import com.soywiz.klogger.Logger
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.defaultResource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.websocket.webSocket
import kotlinx.serialization.builtins.list
import penta.PlayerIds
import penta.util.json
import penta.network.GameSessionInfo
import penta.network.LoginRequest
import penta.network.LoginResponse
import penta.network.ServerStatus
import kotlin.random.Random
import kotlin.IllegalArgumentException

private val logger = Logger("Routes")
fun Application.routes() = routing {
    static("/") {
        resources("static")
        defaultResource("static/index.html")
//        this.contentType(ContentType("text/css"))
    }

    webSocket("ws/lobby") {
        logger.info { "websocket connection opened" }
        val sessionId = (incoming.receive() as Frame.Text).readText()
        val session = SessionController.get(sessionId)
        if (session == null) {
            logger.error { "not authenticated" }
            return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "not authenticated"))
        }

        LobbyHandler.handle(this, session)
    }

    webSocket("/ws/game/{gameId}") {

        logger.info { "websocket connection opened" }
        val sessionId = (incoming.receive() as Frame.Text).readText()
        val session = SessionController.get(sessionId)
        if (session == null) {
            logger.error { "not authenticated" }
            return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "not authenticated"))
        }

        val gameId = call.parameters["gameId"] ?: throw IllegalArgumentException("missing parameter gameId")

        val game = GlobalState.getState().games.find {
            it.serverGameId == gameId
        } ?: run {
            return@webSocket close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "game not found"))
        }

        game.handle(this, session)
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
        // val dbuser = DBUser.getByUserId(loginRequest.userId)
        // val response: LoginResponse = if (dbuser == null) {

//        val db = connect()
        val user: User.RegisteredUser? = UserManager.find(loginRequest.userId) as? User.RegisteredUser
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
                        val randomString = Random.nextInt(2048).toString(16)
                        // create temporary user
                        val tmpUser = User.TemporaryUser(loginRequest.userId)
                        val tmpSession = UserSession(tmpUser.userId)
//                        call.sessions.set(tmpSession)
                        SessionController.set(tmpSession, call)

                        LoginResponse.Success(
                            message = "Welcome ${tmpUser.displayName}"
                        )
                    }
                }
            }

            // create temporary session
        } else {
//            // TODO: covert User
//            val registeredUser = User.RegisteredUser(
//                userId = dbuser.userId,
//                passwordHash = dbuser.passwordHash,
//                displayNameField = dbuser.displayNameField
//            )
            // TODO: retrieve User
            if (loginRequest.password != user.passwordHash) {
                LoginResponse.IncorrectPassword
            } else {
                val authenticatedSession = UserSession(user.userId)
                SessionController.set(authenticatedSession, call)

                LoginResponse.Success(
                    message = "Welcome back ${user.displayName}"
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
        val session = SessionController.get(call)

        if (session == null) {
            call.respondText(
                text = "not logged in",
                status = HttpStatusCode.Unauthorized
            )
        } else {
            call.respondText(
                text = json.stringify(
                    GameSessionInfo.serializer().list,
                    GameController.listActiveGames().map { gameState ->
                        gameState.info
                    }
                ),
                contentType = ContentType.Application.Json
            )
        }
    }

    get("/api/game/create") {
        val session = SessionController.get(call)
//        val session = call.sessions.get<UserSession>()

        if (session == null) {
            call.respondText(
                text = "not logged in",
                status = HttpStatusCode.Unauthorized
            )
        } else {
            val game = GameController.create(session.asUser())

            call.respondText(
                text = json.stringify(
                    GameSessionInfo.serializer(),
                    game.info
                ),
                contentType = ContentType.Application.Json
            )
        }
    }

    get("/api/game/{gameId}/join") {
        val session = SessionController.get(call)

        if (session == null) {
            call.respondText(
                text = "not logged in",
                status = HttpStatusCode.Unauthorized
            )
            return@get
        }
        val gameId = call.parameters["gameId"] ?: throw IllegalArgumentException("missing parameter gameId")
        val game = GameController.get(gameId) ?: throw IllegalArgumentException("no game found with id $gameId")
        val shape = call.parameters["shape"] ?: throw IllegalArgumentException("missing parameter shape")
        val playerId = call.parameters["player"] ?: throw IllegalArgumentException("missing parameter player")
        val player = PlayerIds.valueOf(playerId) ?: throw IllegalArgumentException("no player found with id $playerId")
        game.requestJoin(user = session.asUser(), shape = shape, player = player)

        call.respondText(
            text = json.stringify(
                GameSessionInfo.serializer(),
                game.info
            ),
            contentType = ContentType.Application.Json
        )
    }

    get("/api/game/{gameId}/start") {
        val session = SessionController.get(call)

        if (session == null) {
            call.respondText(
                text = "not logged in",
                status = HttpStatusCode.Unauthorized
            )
            return@get
        }
        val gameId = call.parameters["gameId"] ?: throw IllegalArgumentException("missing parameter gameId")
        val game = GameController.get(gameId) ?: throw IllegalArgumentException("no game found with id $gameId")
        game.requestStart(session.asUser())

        call.respondText(
            text = json.stringify(
                GameSessionInfo.serializer(),
                game.info
            ),
            contentType = ContentType.Application.Json
        )
    }

    get("/whoami") {
        val session = SessionController.get(call)
        call.respondText(
            session.toString()
        )
    }
}
