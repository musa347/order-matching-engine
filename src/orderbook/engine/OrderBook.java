package orderbook.engine;

import orderbook.model.Order;
import orderbook.model.Side;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import java.util.ArrayList;

public class OrderBook {
    private final ConcurrentSkipListMap<Double, PriceLevel> buyOrders = 
        new ConcurrentSkipListMap<>((a, b) -> Double.compare(b, a));
    
    private final ConcurrentSkipListMap<Double, PriceLevel> sellOrders = 
        new ConcurrentSkipListMap<>();
    
    private final AtomicLong matchedOrders = new AtomicLong(0);
    
    public List<Trade> addOrder(Order order) {
        List<Trade> trades = new ArrayList<>();
        
        if (order.side == Side.BUY) {
            trades.addAll(matchBuyOrder(order));
            if (order.quantity > 0) {
                buyOrders.computeIfAbsent(order.price, k -> new PriceLevel()).addOrder(order);
            }
        } else {
            trades.addAll(matchSellOrder(order));
            if (order.quantity > 0) {
                sellOrders.computeIfAbsent(order.price, k -> new PriceLevel()).addOrder(order);
            }
        }
        
        return trades;
    }
    
    private List<Trade> matchBuyOrder(Order buyOrder) {
        List<Trade> trades = new ArrayList<>();
        
        while (buyOrder.quantity > 0 && !sellOrders.isEmpty()) {
            Double bestSellPrice = sellOrders.firstKey();
            if (buyOrder.price < bestSellPrice) break;
            
            PriceLevel priceLevel = sellOrders.get(bestSellPrice);
            Order sellOrder = priceLevel.getFirstOrder();
            
            long tradeQuantity = Math.min(buyOrder.quantity, sellOrder.quantity);
            trades.add(new Trade(buyOrder.id, sellOrder.id, bestSellPrice, tradeQuantity));
            
            buyOrder.quantity -= tradeQuantity;
            sellOrder.quantity -= tradeQuantity;
            matchedOrders.incrementAndGet();
            
            if (sellOrder.quantity == 0) {
                priceLevel.removeFirstOrder();
                if (priceLevel.isEmpty()) {
                    sellOrders.remove(bestSellPrice);
                }
            }
        }
        
        return trades;
    }
    
    private List<Trade> matchSellOrder(Order sellOrder) {
        List<Trade> trades = new ArrayList<>();
        
        while (sellOrder.quantity > 0 && !buyOrders.isEmpty()) {
            Double bestBuyPrice = buyOrders.firstKey();
            if (sellOrder.price > bestBuyPrice) break;
            
            PriceLevel priceLevel = buyOrders.get(bestBuyPrice);
            Order buyOrder = priceLevel.getFirstOrder();
            
            long tradeQuantity = Math.min(sellOrder.quantity, buyOrder.quantity);
            trades.add(new Trade(buyOrder.id, sellOrder.id, bestBuyPrice, tradeQuantity));
            
            sellOrder.quantity -= tradeQuantity;
            buyOrder.quantity -= tradeQuantity;
            matchedOrders.incrementAndGet();
            
            if (buyOrder.quantity == 0) {
                priceLevel.removeFirstOrder();
                if (priceLevel.isEmpty()) {
                    buyOrders.remove(bestBuyPrice);
                }
            }
        }
        
        return trades;
    }
    
    public long getMatchedOrdersCount() {
        return matchedOrders.get();
    }
}