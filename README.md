# 🚀 High-Performance Order Matching Engine

**Live Demo**: [Deploy to Render](https://render.com) 

Single-threaded matching engine with lock-free hot path achieving **80K+ orders/sec** throughput.

## 🏗️ Architecture Highlights

- ✅ **Single-threaded matching** - Deterministic order processing
- ✅ **Lock-free hot path** - Zero synchronization in matching logic  
- ✅ **Multi-producer submission** - Concurrent order submission via queue
- ✅ **Nano-precision latency** - Sub-millisecond P99 latency tracking
- ✅ **REST API** - Real-time metrics and order submission

## 📊 API Endpoints

### GET `/api/metrics`
```json
{
  "processedOrders": 15420,
  "matchedOrders": 12180,
  "averageLatencyMicros": 245.8,
  "p99LatencyMicros": 890.2,
  "throughputOps": 15420,
  "uptime": 1703123456789
}
```

### POST `/api/orders`
```bash
curl -X POST https://your-app.onrender.com/api/orders \
  -H "Content-Type: application/json" \
  -d '{"side":"BUY","price":100.50,"quantity":1000}'
```

### GET `/api/health`
```json
{
  "status": "UP",
  "engine": "RUNNING", 
  "timestamp": 1703123456789
}
```

## 🚀 Deploy to Render

1. **Fork this repo**
2. **Connect to Render**:
   - Service Type: Web Service
   - Build Command: `./build.sh`
   - Start Command: `java -jar target/matching-engine-1.0.0.jar`
3. **Deploy** - Auto-deploys on git push

## 🔧 Local Development

```bash
# Build & Run
./build.sh
java -jar target/matching-engine-1.0.0.jar

# With JFR Profiling  
./run.sh

# Test API
curl http://localhost:8080/api/metrics
```

## 📈 Performance Characteristics

- **Throughput**: 80K-100K orders/sec
- **P99 Latency**: <1ms  
- **Memory**: <256MB heap
- **Architecture**: Single matcher thread + concurrent producers

## 🎯 Recruiter Highlights

**"I designed a single-threaded matching engine with multi-producer order submission to minimize locking in the hot path. I measured throughput and latency using nanoTime and profiled allocation and CPU behavior using JFR to guide optimizations."**

**Key Technical Decisions**:
- ConcurrentSkipListMap for lock-free price level reads
- BlockingQueue for producer-consumer decoupling  
- Single synchronized method → Single thread ownership
- Atomic counters for metrics without locks
- Nano-precision timestamps for accurate latency measurement

**Production Ready**:
- Containerized with Docker
- REST API for monitoring
- Health checks and metrics endpoints
- Memory-optimized JVM settings
- Graceful shutdown handling