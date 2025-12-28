package orderbook;

import orderbook.engine.MatchingEngine;
import orderbook.model.Order;
import orderbook.model.Side;
import orderbook.util.TimeUtil;
import orderbook.util.Logger;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int numThreads = 4;
        int ordersPerThread = 25000;
        
        MatchingEngine engine = new MatchingEngine();
        ExecutorService producers = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        
        Logger.info("Starting single-threaded order matching engine test...");
        long startTime = System.currentTimeMillis();
        
        for (int t = 0; t < numThreads; t++) {
            producers.submit(() -> {
                Random random = new Random();
                for (int i = 0; i < ordersPerThread; i++) {
                    Side side = random.nextBoolean() ? Side.BUY : Side.SELL;
                    double price = 100 + random.nextGaussian() * 5;
                    long quantity = 1 + random.nextInt(100);
                    
                    engine.submitOrder(new Order(side, price, quantity));
                }
                latch.countDown();
            });
        }
        
        latch.await();
        Thread.sleep(1000);
        
        long endTime = System.currentTimeMillis();
        long totalOrders = (long) numThreads * ordersPerThread;
        double durationSec = (endTime - startTime) / 1000.0;
        
        Logger.info("\n=== Performance Results ===");
        Logger.info("Total orders submitted: %d", totalOrders);
        Logger.info("Orders processed: %d", engine.getProcessedOrders());
        Logger.info("Orders matched: %d", engine.getMatchedOrders());
        Logger.info("Duration: %.2f seconds", durationSec);
        Logger.info("Throughput: %.0f orders/sec", totalOrders / durationSec);
        Logger.info("Average latency: %.2f μs", TimeUtil.nanosToMicros((long)engine.getAverageLatencyNs()));
        Logger.info("P99 latency: %.2f μs", TimeUtil.nanosToMicros(engine.getP99LatencyNs()));
        
        engine.shutdown();
        producers.shutdown();
    }
}