//package penta.server.db
//
//import com.mongodb.client.model.Updates.rename
//import kotlinx.serialization.SerialName
//import kotlinx.serialization.Serializable
//import org.litote.kmongo.and
//import org.litote.kmongo.combine
//import org.litote.kmongo.div
//import org.litote.kmongo.eq
//import org.litote.kmongo.ne
//import org.litote.kmongo.set
//import org.litote.kmongo.setTo
//
//@Serializable
//data class DBUser(
//    @SerialName("_id") val userId: String,
//    val passwordHash: String,
//    val displayNameField: String? = null,
//    val score: Score? = null,
//    val version: Int = 3
//) {
//    @Serializable
//    data class Score(
//        val score: Int,
//        val games: List<String> = listOf(),
//        val version: Int = 0
//    )
//
//    suspend fun insertOrReplace() {
//        col.insertOrReplace(DBUser::userId eq userId) { user ->
//            this
//        }
//    }
//
//    companion object {
//        val col = DB.database.getCollection<DBUser>() //KMongo extension method
//
//        suspend fun insert(data: DBUser) {
//            col.insertOne(data)
//        }
//
//        suspend fun getByUserId(userId: String): DBUser? {
//            return col.findOne(DBUser::userId eq userId)
//        }
//
//        suspend fun migrate() {
//            col.updateMany(
//                DBUser::version eq null,
//                DBUser::version.setTo(0)
//            )
//            col.updateMany(
//                DBUser::version eq 0,
//                DBUser::version.setTo(1),
//                DBUser::score.setTo(null)
//            )
//            col.updateMany(
//                DBUser::version eq 1,
//                combine(
//                    set(
//                        DBUser::version.setTo(2)
//                    ),
//                    rename("displaNameField", "displayNameField")
//                )
//            )
//            col.updateMany(
//                DBUser::version eq 2,
//                DBUser::version.setTo(3),
//                DBUser::score.setTo(null)
//            )
//            col.updateMany(
//                and(
//                    DBUser::score ne null,
//                    DBUser::score / Score::version eq null
//                ),
//                (DBUser::score / Score::version).setTo(0), // crash ?
//                (DBUser::score / Score::games).setTo(listOf()) // crash ?
//            )
//            col.updateMany(
//                and(
//                    DBUser::score ne null,
//                    DBUser::score / Score::version eq 0
//                ),
//                (DBUser::score / Score::version).setTo(1), // crash ?
//                (DBUser::score / Score::games).setTo(listOf("")) // crash ?
//            )
//        }
//    }
//}