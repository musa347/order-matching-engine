package orderbook.model;

public class TimedOrder {
    public final Order order;
    public final long enqueueTime;
    
    public TimedOrder(Order order, long enqueueTime) {
        this.order = order;
        this.enqueueTime = enqueueTime;
    }
}