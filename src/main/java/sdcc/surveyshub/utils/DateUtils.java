package sdcc.surveyshub.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateUtils {

    private static final ZoneId ITALY = ZoneId.of("Europe/Rome");

    public static Instant now() {
        return ZonedDateTime.now(ITALY).toInstant();
    }

    public static Instant nowPlusYear(int years) {
        return ZonedDateTime.now(ITALY).plusYears(years).toInstant();
    }

    public static ZonedDateTime toZonedDateTime(Instant instant) {
        return instant.atZone(ITALY);
    }

}

