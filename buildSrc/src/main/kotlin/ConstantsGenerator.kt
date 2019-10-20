import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File
import java.io.Serializable

fun generateConstants(folder: File, pkg: String = "", className: String, configure: ConstantsConfiguration.() -> Unit) {
    val config = ConstantsConfiguration(pkg, className)
    config.configure()

    val constantBuilder =
        TypeSpec.objectBuilder(ClassName(config.pkg, config.className))

    config.fields.forEach { (key, value) ->
        when (value) {
            is String -> {
                constantBuilder.addProperty(
                    PropertySpec.builder(
                        key,
                        String::class,
                        KModifier.CONST
                    )
                        .initializer("%S", value)
                        .build()
                )
            }
            is Int -> {
                constantBuilder.addProperty(
                    PropertySpec.builder(
                        key,
                        Int::class,
                        KModifier.CONST
                    )
                        .initializer("%L", value)
                        .build()
                )
            }
        }
    }

    val source = FileSpec.get(config.pkg, constantBuilder.build())
    source.writeTo(folder)
}

class ConstantsConfiguration(val pkg: String, val className: String) : Serializable {
    var fields: Map<String, Any> = mapOf()
        private set

    fun field(name: String) = ConstantField(name)

    infix fun ConstantField.value(value: String) {
        fields += this.name to value
    }

    infix fun ConstantField.value(value: Int) {
        fields += this.name to value
    }
}

data class ConstantField (
    val name: String
)
