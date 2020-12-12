package server

import io.ktor.application.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.routing.*
import io.ktor.util.*
import io.rsocket.kotlin.ConnectionAcceptor
import io.rsocket.kotlin.ExperimentalMetadataApi
import io.rsocket.kotlin.RSocketRequestHandler
import io.rsocket.kotlin.core.RSocketServer
import io.rsocket.kotlin.core.WellKnownMimeType
import io.rsocket.kotlin.metadata.RoutingMetadata
import io.rsocket.kotlin.metadata.read
import io.rsocket.kotlin.payload.*
import io.rsocket.kotlin.transport.ktor.server.RSocketSupport
import io.rsocket.kotlin.transport.ktor.server.rSocket
import io.rsocket.kotlin.transport.ktor.serverTransport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(KtorExperimentalAPI::class, ExperimentalSerializationApi::class, ExperimentalMetadataApi::class)
fun Application.rsocket() {

    val rSocketServer = RSocketServer()

    fun Payload.route(): String = metadata?.read(RoutingMetadata)?.tags?.first() ?: error("No route provided")

    //create acceptor
    val acceptor = ConnectionAcceptor {
        // parse session from setup payload
        val userName = config.setupPayload.data.readText()

        require(config.payloadMimeType.data == WellKnownMimeType.ApplicationProtoBuf.text) {
            "payload is not protobuf encoded"
        }

        // set up session
        val sessionHolder = SessionHolder(Session("session variables here"))

        RSocketRequestHandler {
            fireAndForget { payload ->
                withContext(sessionHolder) {
                    when(val route = payload.route()) {
//                        "users.deleteMe" -> userApi.deleteMe()

                        else -> error("Wrong route: $route")
                    }
                }
            }
            requestResponse { payload ->
                withContext(sessionHolder) {
                    when (val route = payload.route()) {
//                        "users.getMe"      -> proto.encodeToPayload(userApi.getMe())
//                        "users.all"        -> proto.encodeToPayload(userApi.all())
//
//                        "chats.all"        -> proto.encodeToPayload(chatsApi.all())
//                        "chats.new"        -> proto.decoding<NewChat, Chat>(it) { (name) -> chatsApi.new(name) }
//                        "chats.delete"     -> proto.decoding<DeleteChat>(it) { (id) -> chatsApi.delete(id) }
//
//                        "messages.send"    -> proto.decoding<SendMessage, Message>(it) { (chatId, content) ->
//                            messagesApi.send(chatId, content)
//                        }
//                        "messages.history" -> proto.decoding<HistoryMessages, List<Message>>(it) { (chatId, limit) ->
//                            messagesApi.history(chatId, limit)
//                        }

                        else               -> error("Wrong route: $route")
                    }
                }
            }
            requestStream { payload ->
                when (val route = payload.route()) {
                    "messages.stream" -> {
//                        val (chatId, fromMessageId) = proto.decodeFromPayload<StreamMessages>(payload)
//                        messagesApi.messages(chatId, fromMessageId).map { m -> proto.encodeToPayload(m) }
                        flow<Payload> {
                            buildPayload {
                                data("data")
                                metadata("metadata")
                            }
                        }
                    }

                    else              -> error("Wrong route: $route")
                }.flowOn(sessionHolder)
            }
            requestChannel { initPayload, payloads ->
                when (val route = initPayload.route()) {
                    "messages.channel" -> {
//                        val (chatId, fromMessageId) = proto.decodeFromPayload<StreamMessages>(initPayload)
//                        messagesApi.messages(chatId, fromMessageId, payloads).map { m -> proto.encodeToPayload(m) }
                        flow<Payload> {
                            buildPayload {
                                data("data")
                                metadata("metadata")
                            }
                        }
                    }
                    else              -> error("Wrong route: $route")
                }.flowOn(sessionHolder)
            }
        }
    }

    //start TCP server
    val tcpTransport = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().serverTransport(port = 8000)
    val rsocketServerJob = rSocketServer.bind(tcpTransport, acceptor)

    install(RSocketSupport) {
        //configure rSocket server (all values have defaults)
        server = rSocketServer
    }

    routing {
        rSocket(path = "rsocket", acceptor = acceptor)
    }

    environment.monitor.subscribe(ApplicationStopped) {
        println("stopping rsocket tcp server")
        rsocketServerJob.cancel()
    }
}