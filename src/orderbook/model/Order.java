package orderbook.model;

import java.util.concurrent.atomic.AtomicLong;

public class Order {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);
    
    public final long id;
    public final Side side;
    public final OrderType type;
    public final double price;
    public volatile long quantity;
    public final long timestamp;
    
    public Order(Side side, double price, long quantity) {
        this.id = ID_GENERATOR.incrementAndGet();
        this.side = side;
        this.type = OrderType.LIMIT;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = System.nanoTime();
    }
    
    // Backward compatibility constructor
    public Order(boolean isBuy, double price, long quantity) {
        this(isBuy ? Side.BUY : Side.SELL, price, quantity);
    }
}