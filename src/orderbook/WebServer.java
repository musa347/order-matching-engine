package orderbook;

import orderbook.api.OrderBookAPI;
import orderbook.model.Order;
import orderbook.model.Side;
import orderbook.util.Logger;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebServer {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        
        OrderBookAPI api = new OrderBookAPI(port);
        api.start();
        
        // Start background order generation for demo
        startDemoTrading(api);
        
        Runtime.getRuntime().addShutdownHook(new Thread(api::stop));
        
        Logger.info(" Order Matching Engine deployed on port " + port);
        Logger.info(" Visit /api/metrics for real-time performance data");
    }
    
    private static void startDemoTrading(OrderBookAPI api) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        Random random = new Random();
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Side side = random.nextBoolean() ? Side.BUY : Side.SELL;
                double price = 100 + random.nextGaussian() * 2;
                long quantity = 1 + random.nextInt(50);
                
                // Simulate order submission via internal API
                Order order = new Order(side, price, quantity);
                // api.engine.submitOrder(order); // Would need to expose engine
            } catch (Exception e) {
                // Ignore demo errors
            }
        }, 1, 100, TimeUnit.MILLISECONDS);
    }
}