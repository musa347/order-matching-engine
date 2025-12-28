package orderbook.engine;

import orderbook.model.Order;
import orderbook.model.TimedOrder;
import orderbook.concurrency.OrderDispatcher;
import orderbook.concurrency.EngineWorker;
import orderbook.metrics.LatencyRecorder;
import orderbook.metrics.ThroughputCounter;
import orderbook.util.TimeUtil;

public class MatchingEngine {
    private final OrderBook orderBook = new OrderBook();
    private final LatencyRecorder latencyRecorder = new LatencyRecorder(10000);
    private final ThroughputCounter throughputCounter = new ThroughputCounter();
    private final OrderDispatcher dispatcher;
    private final Thread engineThread;
    
    public MatchingEngine() {
        this.dispatcher = new OrderDispatcher(orderBook, latencyRecorder, throughputCounter);
        this.engineThread = new Thread(new EngineWorker(dispatcher), "matching-engine");
        this.engineThread.start();
    }
    
    public void submitOrder(Order order) {
        long enqueueTime = TimeUtil.nanoTime();
        dispatcher.submit(new TimedOrder(order, enqueueTime));
    }
    
    public void shutdown() {
        engineThread.interrupt();
        try {
            engineThread.join(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public long getProcessedOrders() {
        return throughputCounter.getCount();
    }
    
    public long getMatchedOrders() {
        return orderBook.getMatchedOrdersCount();
    }
    
    public double getAverageLatencyNs() {
        return latencyRecorder.getAverageLatencyNs(throughputCounter.getCount());
    }
    
    public long getP99LatencyNs() {
        return latencyRecorder.getP99LatencyNs();
    }
}