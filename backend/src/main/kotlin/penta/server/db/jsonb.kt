package penta.server.db

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.StringColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject
import java.sql.PreparedStatement

/**
 * Created by quangio.
 */

fun <T : Any> Table.jsonb(name: String, json: kotlinx.serialization.json.Json, serializer: KSerializer<T>): Column<T>
    = registerColumn(name, Json(json, serializer))
fun Table.jsonb2(name: String): Column<String>
    = registerColumn(name, JsonString())


private class JsonString(): ColumnType() {
    override fun sqlType() = "jsonb"
    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        val obj = PGobject()
        obj.type = "jsonb"
        obj.value = value as String
        stmt.set(index, obj)
    }

    override fun valueToString(value: Any?): String {
        if(value is PGobject) {
            return value.value
        }
        return value as String
    }
    override fun valueFromDB(value: Any): Any {
        if (value !is PGobject) {
            // We didn't receive a PGobject (the format of stuff actually coming from the DB).
            // In that case "value" should already be an object of type T.
            return value
        }

        return value.value
    }
    override fun nonNullValueToString(value: Any): String {
        if(value is PGobject) {
            return value.value
        }
        return value as String
    }
}


private class Json<out T : Any>(private val json: kotlinx.serialization.json.Json, private val serializer: KSerializer<T>) : ColumnType() {
    override fun sqlType() = "jsonb"

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        val obj = PGobject()
        obj.type = "jsonb"
        obj.value = value as String
        stmt.set(index, obj)
    }

    override fun valueFromDB(value: Any): Any {
        if (value !is PGobject) {
            // We didn't receive a PGobject (the format of stuff actually coming from the DB).
            // In that case "value" should already be an object of type T.
            return value
        }

        // We received a PGobject, deserialize its String value.
        return try {
            json.parse(serializer, value.value)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Can't parse JSON: $value")
        }
    }

    override fun notNullValueToDB(value: Any): Any = json.stringify(serializer, value as T)
    override fun nonNullValueToString(value: Any): String {
        println("serializing: $value")
        return "'${json.stringify(serializer, value as T)}'"
    }
}