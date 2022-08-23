package dev.sotoestevez.coffeeservice.web.mappers;

import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class DateMapper {

    public OffsetDateTime asOffsetDateTime(Timestamp time) {
        if (time == null) {
            return null;
        }
        return OffsetDateTime.of(time.toLocalDateTime(), ZoneOffset.UTC);
    }

    public Timestamp asTimestamp(OffsetDateTime time) {
        if (time == null) {
            return null;
        }
        return Timestamp.valueOf(time.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
    }

}
