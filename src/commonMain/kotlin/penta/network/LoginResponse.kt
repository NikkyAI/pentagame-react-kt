package penta.network

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModuleBuilder

@Polymorphic
@Serializable(PolymorphicSerializer::class)
sealed class LoginResponse {
    interface Failure

    @Serializable
    data class Success(
       val message: String
    ) : LoginResponse()

    @Serializable
    class IncorrectPassword: LoginResponse(), Failure

    companion object {
        fun install(builder: SerializersModuleBuilder) {
            builder.polymorphic<LoginResponse> {
                Success::class with Success.serializer()
                IncorrectPassword::class with IncorrectPassword.serializer()
            }
        }
    }
}