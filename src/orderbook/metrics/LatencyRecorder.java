package orderbook.metrics;

import orderbook.util.TimeUtil;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class LatencyRecorder {
    private final AtomicLong totalLatency = new AtomicLong(0);
    private final ConcurrentLinkedQueue<Long> latencies = new ConcurrentLinkedQueue<>();
    private final int maxSamples;
    
    public LatencyRecorder(int maxSamples) {
        this.maxSamples = maxSamples;
    }
    
    public void recordLatency(long startTime) {
        long latency = TimeUtil.nanoTime() - startTime;
        totalLatency.addAndGet(latency);
        latencies.offer(latency);
        
        if (latencies.size() > maxSamples) {
            latencies.poll();
        }
    }
    
    public double getAverageLatencyNs(long totalOperations) {
        return totalOperations > 0 ? (double) totalLatency.get() / totalOperations : 0;
    }
    
    public long getP99LatencyNs() {
        Long[] latencyArray = latencies.toArray(new Long[0]);
        if (latencyArray.length == 0) return 0;
        
        java.util.Arrays.sort(latencyArray);
        int p99Index = (int) Math.ceil(latencyArray.length * 0.99) - 1;
        return latencyArray[Math.max(0, p99Index)];
    }
}