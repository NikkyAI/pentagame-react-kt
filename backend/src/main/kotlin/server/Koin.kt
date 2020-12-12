package server

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import org.koin.dsl.module
import server.db.Database
import server.db.InstantLongColumnAdapter
import server.db.Users

fun databaseModule() = module {
    single<SqlDriver> {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    }
    single<Database> {
        Database(
            driver = get<SqlDriver>(),
            usersAdapter = Users.Adapter(
                apiTokenExpirationAdapter = InstantLongColumnAdapter
            )
        )
    }
}