package com.example.cod;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Utils {

    private static Utils instance = null;

    private Utils() {}

    public static synchronized Utils getInstance() {
        if (instance == null) {
            instance = new Utils();
        }
        return instance;
    }

    public String getTheTimeNow(Long minusTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        ZoneId zone = ZoneId.of("Europe/Berlin");

        long timeNow = System.currentTimeMillis() / 1000L;
        timeNow -= minusTime;

        ZonedDateTime dateTime = Instant.ofEpochSecond(timeNow).atZone(zone);
        return dateTime.format(formatter);
    }
}
