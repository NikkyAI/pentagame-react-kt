package penta.server.db

import com.mongodb.ConnectionString
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

object DB {
    val client: CoroutineClient = System.getenv()["MONGODB_URI"]?.let {
        KMongo.createClient(ConnectionString(it)).coroutine
    } ?: run {
        KMongo.createClient().coroutine
    }
    //    val coroutineClient = client.coroutine
    val database = client.getDatabase("test") //normal java driver usage
    //
    val version: Int = 0

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        testDb()
    }

    suspend fun testDb() {
        DBUser.migrate()

//        DBUser("nikky", "password", "abcd").insertOrReplace()
//        DBUser("tempuser", "").insertOrReplace()
//        DBUser("tempuser2", "").insertOrReplace()

        val nikky = DBUser.getByUserId("nikky")

        println("user: $nikky")

//        nikky?.let {
//            it.copy(score = DBUser.ScoreInfo(100))
//        }?.insertOrReplace()

//        db.designs.updateMany({}, { $set: { "dateCreated": ISODate() } })
    }
}