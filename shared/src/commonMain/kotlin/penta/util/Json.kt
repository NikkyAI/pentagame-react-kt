package penta.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import penta.SerialNotation
import penta.network.GlobalEvent
import penta.network.LoginResponse

val json = Json(
    JsonConfiguration(
        unquoted = false,
        allowStructuredMapKeys = false, //true,
        prettyPrint = false,
        classDiscriminator = "type"
    ), context = SerializersModule {
        SerialNotation.install(this)
        LoginResponse.install(this)
        GlobalEvent.install(this)
    })