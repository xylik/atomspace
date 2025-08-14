package org.atomspace.values;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * StreamValue represents dynamic, flowing data in AtomSpace.
 * 
 * This is one of the most innovative aspects of AtomSpace - Values can represent
 * not just static data, but dynamic streams. This enables the graph structure
 * to act as "plumbing" while Values flow through like data through pipes.
 * 
 * StreamValues can be used for:
 * - Real-time sensor data
 * - Live video/audio feeds  
 * - Computed values that update over time
 * - Neural network activations during inference
 */
public class StreamValue implements Value {
    private final BlockingQueue<Value> buffer;
    private final AtomicBoolean isActive;
    private final Thread producerThread;
    
    /**
     * Create a StreamValue with a producer function that generates values
     */
    public StreamValue(Supplier<Value> producer, int bufferSize) {
        this.buffer = new LinkedBlockingQueue<>(bufferSize);
        this.isActive = new AtomicBoolean(true);
        
        // Start producer thread
        this.producerThread = new Thread(() -> {
            while (isActive.get()) {
                try {
                    Value value = producer.get();
                    if (value != null) {
                        // If buffer is full, remove oldest value
                        if (!buffer.offer(value)) {
                            buffer.poll(); // Remove oldest
                            buffer.offer(value); // Add newest
                        }
                    }
                    Thread.sleep(10); // Small delay to prevent busy waiting
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in StreamValue producer: " + e.getMessage());
                }
            }
        });
        this.producerThread.setDaemon(true);
        this.producerThread.start();
    }
    
    /**
     * Get the most recent value from the stream (non-blocking)
     */
    public Value getLatest() {
        return buffer.peek();
    }
    
    /**
     * Get the next value from the stream (blocking)
     */
    public Value getNext() throws InterruptedException {
        return buffer.take();
    }
    
    /**
     * Get the next value with timeout
     */
    public Value getNext(long timeoutMs) throws InterruptedException {
        return buffer.poll(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
    
    /**
     * Check if stream is still active
     */
    public boolean isActive() {
        return isActive.get();
    }
    
    /**
     * Get current buffer size
     */
    public int getBufferSize() {
        return buffer.size();
    }
    
    /**
     * Stop the stream
     */
    public void stop() {
        isActive.set(false);
        producerThread.interrupt();
    }
    
    @Override
    public ValueType getType() {
        return ValueType.STREAM_VALUE;
    }
    
    @Override
    public Value clone() {
        throw new UnsupportedOperationException("StreamValue cannot be cloned");
    }
    
    @Override
    public boolean equals(Object obj) {
        // StreamValues are unique by identity
        return this == obj;
    }
    
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
    
    @Override
    public String toString() {
        Value latest = getLatest();
        return String.format("StreamValue(active=%s, buffer=%d, latest=%s)", 
                           isActive.get(), buffer.size(), latest);
    }
    
    /**
     * Create a StreamValue that generates random FloatValues
     */
    public static StreamValue randomFloats(int vectorSize, int bufferSize) {
        java.util.Random random = new java.util.Random();
        return new StreamValue(() -> {
            double[] values = new double[vectorSize];
            for (int i = 0; i < vectorSize; i++) {
                values[i] = random.nextGaussian();
            }
            return new FloatValue(values);
        }, bufferSize);
    }
    
    /**
     * Create a StreamValue that generates TruthValues with varying strength
     */
    public static StreamValue varyingTruth(double baseStrength, double variance, int bufferSize) {
        java.util.Random random = new java.util.Random();
        return new StreamValue(() -> {
            double strength = Math.max(0.0, Math.min(1.0, 
                baseStrength + (random.nextGaussian() * variance)));
            double confidence = 0.8 + random.nextDouble() * 0.2; // High confidence
            return new TruthValue(strength, confidence);
        }, bufferSize);
    }
}