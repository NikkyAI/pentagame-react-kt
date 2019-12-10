package penta.server.db

import org.bson.conversions.Bson
import org.litote.kmongo.coroutine.CoroutineCollection

suspend fun <T : Any> CoroutineCollection<T>.insertOrReplace(filter: Bson, update: (T?) -> T) {
    val entry = findOne(filter)
    val target = update(entry)
    if (entry == null) {
        insertOne(target)
    } else {
        replaceOne(filter, target)
    }
}