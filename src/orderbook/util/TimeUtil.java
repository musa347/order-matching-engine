package orderbook.util;

public class TimeUtil {
    public static long nanoTime() {
        return System.nanoTime();
    }
    
    public static double nanosToMicros(long nanos) {
        return nanos / 1000.0;
    }
}