package orderbook.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    public static void info(String message) {
        System.out.printf("[%s] INFO: %s%n", LocalDateTime.now().format(FORMATTER), message);
    }
    
    public static void info(String format, Object... args) {
        info(String.format(format, args));
    }
}