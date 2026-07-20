package com.deepgram.echofault.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Inbound control message from the browser (JSON over WebSocket).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InboundMessage {

    private String type;
    private Double packetLossPercent;
    private Long latencyMs;
    private Double silencePercent;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getPacketLossPercent() {
        return packetLossPercent;
    }

    public void setPacketLossPercent(Double packetLossPercent) {
        this.packetLossPercent = packetLossPercent;
    }

    public Long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public Double getSilencePercent() {
        return silencePercent;
    }

    public void setSilencePercent(Double silencePercent) {
        this.silencePercent = silencePercent;
    }
}
