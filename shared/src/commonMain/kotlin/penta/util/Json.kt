package penta.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import penta.network.GameEvent
import penta.network.GlobalEvent
import penta.network.LoginResponse

val json = Json(
    JsonConfiguration(
        unquoted = false,
        allowStructuredMapKeys = true,
        prettyPrint = false,
        classDiscriminator = "type"
    ), context = SerializersModule {
        GameEvent.install(this)
        LoginResponse.install(this)
        GlobalEvent.install(this)
    })