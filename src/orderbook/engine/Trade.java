package orderbook.engine;

public class Trade {
    public final long buyOrderId;
    public final long sellOrderId;
    public final double price;
    public final long quantity;
    public final long timestamp;
    
    public Trade(long buyOrderId, long sellOrderId, double price, long quantity) {
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = System.nanoTime();
    }
}