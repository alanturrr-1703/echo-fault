package com.deepgram.echofault.model;

/**
 * Runtime chaos experiment configuration, adjustable via the dashboard.
 */
public class ChaosConfig {

    private volatile double packetLossPercent = 0;
    private volatile long latencyMs = 0;
    private volatile double silencePercent = 0;

    public double getPacketLossPercent() {
        return packetLossPercent;
    }

    public void setPacketLossPercent(double packetLossPercent) {
        this.packetLossPercent = packetLossPercent;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public double getSilencePercent() {
        return silencePercent;
    }

    public void setSilencePercent(double silencePercent) {
        this.silencePercent = silencePercent;
    }

    public ChaosConfig copy() {
        ChaosConfig copy = new ChaosConfig();
        copy.packetLossPercent = packetLossPercent;
        copy.latencyMs = latencyMs;
        copy.silencePercent = silencePercent;
        return copy;
    }
}
