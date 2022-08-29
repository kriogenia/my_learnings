package dev.sotoestevez.orderservice.web.mappers

import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class DateMapper {

    fun map(timestamp: Timestamp): OffsetDateTime {
        return OffsetDateTime.of(timestamp.toLocalDateTime(), ZoneOffset.UTC)
    }

    fun map(offsetDateTime: OffsetDateTime): Timestamp {
        return Timestamp.from(offsetDateTime.toInstant())
    }

}