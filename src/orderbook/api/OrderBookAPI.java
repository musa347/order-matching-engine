package orderbook.api;

import orderbook.engine.MatchingEngine;
import orderbook.model.Order;
import orderbook.model.Side;
import orderbook.util.TimeUtil;
import orderbook.util.JsonUtil;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.HashMap;

public class OrderBookAPI {
    private final MatchingEngine engine;
    private final HttpServer server;
    
    public OrderBookAPI(int port) throws IOException {
        this.engine = new MatchingEngine();
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        setupRoutes();
    }
    
    private void setupRoutes() {
        server.createContext("/", this::handleRoot);
        server.createContext("/api/orders", this::handleOrders);
        server.createContext("/api/metrics", this::handleMetrics);
        server.createContext("/api/health", this::handleHealth);
    }
    
    private void handleRoot(HttpExchange exchange) throws IOException {
        String html = "<!DOCTYPE html><html><head>" +
            "<title>Order Matching Engine | Live Trading</title>" +
            "<meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<style>" +
            "*{margin:0;padding:0;box-sizing:border-box}" +
            "body{font-family:'Segoe UI',Arial,sans-serif;background:#0d1421;color:#fff;overflow-x:hidden}" +
            ".header{background:linear-gradient(135deg,#1e3a8a,#3b82f6);padding:20px;text-align:center;box-shadow:0 4px 20px rgba(0,0,0,0.3)}" +
            ".header h1{font-size:2.5em;margin-bottom:10px;text-shadow:2px 2px 4px rgba(0,0,0,0.5)}" +
            ".header p{font-size:1.2em;opacity:0.9}" +
            ".container{max-width:1400px;margin:0 auto;padding:20px;display:grid;grid-template-columns:1fr 1fr;gap:20px}" +
            ".panel{background:linear-gradient(145deg,#1f2937,#374151);border-radius:12px;padding:20px;box-shadow:0 8px 32px rgba(0,0,0,0.3);border:1px solid #4b5563}" +
            ".panel h2{color:#60a5fa;margin-bottom:15px;font-size:1.4em;border-bottom:2px solid #3b82f6;padding-bottom:8px}" +
            ".metrics{display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:15px;margin-bottom:20px}" +
            ".metric{background:linear-gradient(135deg,#065f46,#059669);padding:15px;border-radius:8px;text-align:center;border:1px solid #10b981}" +
            ".metric-value{font-size:2em;font-weight:bold;color:#34d399;text-shadow:0 0 10px rgba(52,211,153,0.5)}" +
            ".metric-label{font-size:0.9em;color:#a7f3d0;margin-top:5px}" +
            ".order-form{display:grid;gap:15px}" +
            ".form-group{display:flex;flex-direction:column;gap:5px}" +
            ".form-group label{color:#9ca3af;font-weight:500}" +
            ".form-group input,.form-group select{padding:12px;border:1px solid #4b5563;border-radius:6px;background:#374151;color:#fff;font-size:1em}" +
            ".form-group input:focus,.form-group select:focus{outline:none;border-color:#3b82f6;box-shadow:0 0 0 3px rgba(59,130,246,0.1)}" +
            ".btn{padding:15px 30px;background:linear-gradient(135deg,#dc2626,#ef4444);color:#fff;border:none;border-radius:8px;font-size:1.1em;font-weight:600;cursor:pointer;transition:all 0.3s ease;text-transform:uppercase;letter-spacing:1px}" +
            ".btn:hover{background:linear-gradient(135deg,#b91c1c,#dc2626);transform:translateY(-2px);box-shadow:0 8px 25px rgba(220,38,38,0.4)}" +
            ".btn.buy{background:linear-gradient(135deg,#059669,#10b981)}" +
            ".btn.buy:hover{background:linear-gradient(135deg,#047857,#059669)}" +
            ".status{padding:10px;border-radius:6px;margin-top:10px;text-align:center;font-weight:500}" +
            ".status.success{background:rgba(16,185,129,0.2);border:1px solid #10b981;color:#34d399}" +
            ".status.error{background:rgba(239,68,68,0.2);border:1px solid #ef4444;color:#fca5a5}" +
            ".live-indicator{display:inline-block;width:12px;height:12px;background:#10b981;border-radius:50%;margin-right:8px;animation:pulse 2s infinite}" +
            "@keyframes pulse{0%,100%{opacity:1}50%{opacity:0.5}}" +
            "@media (max-width:768px){.container{grid-template-columns:1fr;gap:15px}.header h1{font-size:2em}}" +
            "</style></head><body>" +
            "<div class='header'><h1>Order Matching Engine</h1><p><span class='live-indicator'></span>Live Trading System | Single-Threaded | Lock-Free</p></div>" +
            "<div class='container'>" +
            "<div class='panel'><h2>Live Performance Metrics</h2><div class='metrics' id='metrics'></div></div>" +
            "<div class='panel'><h2>Submit Order</h2>" +
            "<form class='order-form' onsubmit='submitOrder(event)'>" +
            "<div class='form-group'><label>Side</label><select id='side'><option value='BUY'>BUY</option><option value='SELL'>SELL</option></select></div>" +
            "<div class='form-group'><label>Price ($)</label><input type='number' id='price' step='0.01' value='100.00' required></div>" +
            "<div class='form-group'><label>Quantity</label><input type='number' id='quantity' value='1000' required></div>" +
            "<button type='submit' class='btn' id='submitBtn'>Submit Order</button>" +
            "</form><div id='orderStatus'></div></div></div>" +
            "<script>" +
            "async function loadMetrics(){try{const r=await fetch('/api/metrics');const m=await r.json();" +
            "document.getElementById('metrics').innerHTML=" +
            "`<div class='metric'><div class='metric-value'>${m.processedOrders}</div><div class='metric-label'>Orders Processed</div></div>" +
            "<div class='metric'><div class='metric-value'>${m.matchedOrders}</div><div class='metric-label'>Orders Matched</div></div>" +
            "<div class='metric'><div class='metric-value'>${m.averageLatencyMicros.toFixed(1)}μs</div><div class='metric-label'>Avg Latency</div></div>" +
            "<div class='metric'><div class='metric-value'>${m.p99LatencyMicros.toFixed(1)}μs</div><div class='metric-label'>P99 Latency</div></div>`" +
            "}catch(e){console.error('Failed to load metrics',e)}}" +
            "async function submitOrder(e){e.preventDefault();const btn=document.getElementById('submitBtn');const status=document.getElementById('orderStatus');" +
            "btn.disabled=true;btn.textContent='Submitting...';try{const order={side:document.getElementById('side').value," +
            "price:parseFloat(document.getElementById('price').value),quantity:parseInt(document.getElementById('quantity').value)};" +
            "const r=await fetch('/api/orders',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(order)});" +
            "const result=await r.json();if(r.ok){status.innerHTML=`<div class='status success'>Order ${result.orderId} submitted successfully!</div>`;" +
            "loadMetrics()}else{status.innerHTML=`<div class='status error'>${result.error}</div>`}}catch(e){" +
            "status.innerHTML=`<div class='status error'>Network error</div>`}finally{btn.disabled=false;btn.textContent='Submit Order'}}" +
            "loadMetrics();setInterval(loadMetrics,2000);" +
            "document.getElementById('side').onchange=function(){const btn=document.getElementById('submitBtn');" +
            "btn.className=this.value==='BUY'?'btn buy':'btn';btn.textContent=this.value+' Order'}" +
            "</script></body></html>";
        sendResponse(exchange, 200, html, "text/html");
    }
    
    private void handleOrders(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            try {
                String body = readBody(exchange);
                // Simple JSON parsing for {"side":"BUY","price":100.5,"quantity":1000}
                Side side = body.contains("\"BUY\"") ? Side.BUY : Side.SELL;
                double price = parseDouble(body, "price");
                long quantity = parseLong(body, "quantity");
                
                Order order = new Order(side, price, quantity);
                engine.submitOrder(order);
                
                Map<String, Object> response = new HashMap<>();
                response.put("orderId", order.id);
                response.put("status", "submitted");
                response.put("timestamp", order.timestamp);
                
                sendResponse(exchange, 201, JsonUtil.toJson(response), "application/json");
            } catch (Exception e) {
                sendResponse(exchange, 400, JsonUtil.error("Invalid order format"), "application/json");
            }
        } else {
            sendResponse(exchange, 405, JsonUtil.error("Method not allowed"), "application/json");
        }
    }
    
    private void handleMetrics(HttpExchange exchange) throws IOException {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("processedOrders", engine.getProcessedOrders());
        metrics.put("matchedOrders", engine.getMatchedOrders());
        metrics.put("averageLatencyMicros", TimeUtil.nanosToMicros((long)engine.getAverageLatencyNs()));
        metrics.put("p99LatencyMicros", TimeUtil.nanosToMicros(engine.getP99LatencyNs()));
        metrics.put("throughputOps", engine.getProcessedOrders());
        metrics.put("uptime", System.currentTimeMillis());
        
        sendResponse(exchange, 200, JsonUtil.toJson(metrics), "application/json");
    }
    
    private void handleHealth(HttpExchange exchange) throws IOException {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("engine", "RUNNING");
        health.put("timestamp", System.currentTimeMillis());
        
        sendResponse(exchange, 200, JsonUtil.toJson(health), "application/json");
    }
    
    private double parseDouble(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern) + pattern.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        return Double.parseDouble(json.substring(start, end).trim());
    }
    
    private long parseLong(String json, String key) {
        return (long) parseDouble(json, key);
    }
    
    private String readBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            return reader.lines().reduce("", (a, b) -> a + b);
        }
    }
    
    private void sendResponse(HttpExchange exchange, int code, String response, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(code, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
    
    public void start() {
        server.start();
        System.out.println("Order Matching Engine API started on port " + server.getAddress().getPort());
    }
    
    public void stop() {
        engine.shutdown();
        server.stop(0);
    }
}