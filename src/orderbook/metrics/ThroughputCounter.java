package orderbook.metrics;

import java.util.concurrent.atomic.AtomicLong;

public class ThroughputCounter {
    private final AtomicLong processedOrders = new AtomicLong(0);
    
    public void increment() {
        processedOrders.incrementAndGet();
    }
    
    public long getCount() {
        return processedOrders.get();
    }
}