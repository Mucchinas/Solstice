package io.summertime.core.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static void info(String message) {
        log("INFO", message);
    }

    public static void warn(String message) {
        log("WARN", message);
    }

    public static void error(String message, Throwable throwable) {
        log("ERROR", message);
        if (throwable != null) {
            throwable.printStackTrace(System.err);
        }
    }

    private static void log(String level, String message) {
        String time = TIME_FORMATTER.format(LocalDateTime.now());
        System.out.println(String.format("[%s] %s - %s", time, level, message));
    }
}
