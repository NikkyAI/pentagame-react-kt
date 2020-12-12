package server.db

import com.squareup.sqldelight.ColumnAdapter
import kotlinx.datetime.Instant

object InstantLongColumnAdapter: ColumnAdapter<Instant, Long> {
    override fun decode(databaseValue: Long): Instant {
        return Instant.fromEpochSeconds(databaseValue)
    }

    override fun encode(value: Instant): Long {
        return value.epochSeconds
    }
}