package penta.network

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModuleBuilder
import penta.util.ObjectSerializer

@Polymorphic
@Serializable(PolymorphicSerializer::class)
sealed class LoginResponse {
    interface Failure

    @Serializable
    data class Success(
       val message: String
    ) : LoginResponse()

    @Serializable
    class UserIdRejected(
        val reason: String
    ): LoginResponse(), Failure

    object IncorrectPassword: LoginResponse(), Failure

    companion object {
        fun install(builder: SerializersModuleBuilder) {
            builder.polymorphic<LoginResponse> {
                Success::class with Success.serializer()
                UserIdRejected::class with UserIdRejected.serializer()
                IncorrectPassword::class with ObjectSerializer(IncorrectPassword)
            }
        }
    }
}