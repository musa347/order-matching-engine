package orderbook.engine;

import orderbook.model.Order;
import java.util.ArrayList;
import java.util.List;

public class PriceLevel {
    private final List<Order> orders = new ArrayList<>();
    
    public void addOrder(Order order) {
        orders.add(order);
    }
    
    public Order getFirstOrder() {
        return orders.isEmpty() ? null : orders.get(0);
    }
    
    public void removeFirstOrder() {
        if (!orders.isEmpty()) {
            orders.remove(0);
        }
    }
    
    public boolean isEmpty() {
        return orders.isEmpty();
    }
}