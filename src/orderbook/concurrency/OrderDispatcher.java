package orderbook.concurrency;

import orderbook.model.TimedOrder;
import orderbook.engine.OrderBook;
import orderbook.engine.Trade;
import orderbook.metrics.LatencyRecorder;
import orderbook.metrics.ThroughputCounter;
import orderbook.util.TimeUtil;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class OrderDispatcher {
    private final BlockingQueue<TimedOrder> orderQueue = new LinkedBlockingQueue<>();
    private final OrderBook orderBook;
    private final LatencyRecorder latencyRecorder;
    private final ThroughputCounter throughputCounter;
    
    public OrderDispatcher(OrderBook orderBook, LatencyRecorder latencyRecorder, ThroughputCounter throughputCounter) {
        this.orderBook = orderBook;
        this.latencyRecorder = latencyRecorder;
        this.throughputCounter = throughputCounter;
    }
    
    public void submit(TimedOrder timedOrder) {
        orderQueue.offer(timedOrder);
    }
    
    public void processOrders() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                TimedOrder timedOrder = orderQueue.take();
                long latency = TimeUtil.nanoTime() - timedOrder.enqueueTime;
                
                List<Trade> trades = orderBook.addOrder(timedOrder.order);
                
                latencyRecorder.recordLatency(timedOrder.enqueueTime);
                throughputCounter.increment();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}