package penta.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import penta.network.GameEvent
import penta.network.LobbyEvent
import penta.network.LoginResponse

val json = Json(
    JsonConfiguration(
        unquoted = false,
        allowStructuredMapKeys = false, //true,
        prettyPrint = false,
        classDiscriminator = "type"
    ), context = SerializersModule {
        GameEvent.install(this)
        SessionEvent.install(this)
        LoginResponse.install(this)
        LobbyEvent.install(this)
    })