package com.deepgram.echofault.metrics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Per-session counters for chaos experiment observability.
 */
public class StreamMetrics {

    private final AtomicLong packetsReceived = new AtomicLong();
    private final AtomicLong packetsForwarded = new AtomicLong();
    private final AtomicLong packetsDropped = new AtomicLong();
    private final AtomicLong packetsSilenced = new AtomicLong();

    public AtomicLong getPacketsReceived() {
        return packetsReceived;
    }

    public AtomicLong getPacketsForwarded() {
        return packetsForwarded;
    }

    public AtomicLong getPacketsDropped() {
        return packetsDropped;
    }

    public AtomicLong getPacketsSilenced() {
        return packetsSilenced;
    }

    public void recordReceived() {
        packetsReceived.incrementAndGet();
    }

    public void recordForwarded() {
        packetsForwarded.incrementAndGet();
    }

    public void recordDropped() {
        packetsDropped.incrementAndGet();
    }

    public void recordSilenced() {
        packetsSilenced.incrementAndGet();
    }
}
