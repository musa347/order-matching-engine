package orderbook.concurrency;

import orderbook.concurrency.OrderDispatcher;

public class EngineWorker implements Runnable {
    private final OrderDispatcher dispatcher;
    
    public EngineWorker(OrderDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }
    
    @Override
    public void run() {
        dispatcher.processOrders();
    }
}