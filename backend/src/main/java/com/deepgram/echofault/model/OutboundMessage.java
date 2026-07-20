package com.deepgram.echofault.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Outbound WebSocket message sent to the browser.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutboundMessage {

    private String type;
    private String transcript;
    private Boolean isFinal;
    private Double confidence;
    private String connectionStatus;
    private Double packetLossPercent;
    private Long latencyMs;
    private Double silencePercent;
    private Long packetsReceived;
    private Long packetsForwarded;
    private Long packetsDropped;
    private Long packetsSilenced;
    private String error;

    public static Builder builder() {
        return new Builder();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public Boolean getIsFinal() {
        return isFinal;
    }

    public void setIsFinal(Boolean isFinal) {
        this.isFinal = isFinal;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
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

    public Long getPacketsReceived() {
        return packetsReceived;
    }

    public void setPacketsReceived(Long packetsReceived) {
        this.packetsReceived = packetsReceived;
    }

    public Long getPacketsForwarded() {
        return packetsForwarded;
    }

    public void setPacketsForwarded(Long packetsForwarded) {
        this.packetsForwarded = packetsForwarded;
    }

    public Long getPacketsDropped() {
        return packetsDropped;
    }

    public void setPacketsDropped(Long packetsDropped) {
        this.packetsDropped = packetsDropped;
    }

    public Long getPacketsSilenced() {
        return packetsSilenced;
    }

    public void setPacketsSilenced(Long packetsSilenced) {
        this.packetsSilenced = packetsSilenced;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public static final class Builder {
        private final OutboundMessage message = new OutboundMessage();

        public Builder type(String type) {
            message.type = type;
            return this;
        }

        public Builder transcript(String transcript) {
            message.transcript = transcript;
            return this;
        }

        public Builder isFinal(Boolean isFinal) {
            message.isFinal = isFinal;
            return this;
        }

        public Builder confidence(Double confidence) {
            message.confidence = confidence;
            return this;
        }

        public Builder connectionStatus(String connectionStatus) {
            message.connectionStatus = connectionStatus;
            return this;
        }

        public Builder packetLossPercent(Double packetLossPercent) {
            message.packetLossPercent = packetLossPercent;
            return this;
        }

        public Builder latencyMs(Long latencyMs) {
            message.latencyMs = latencyMs;
            return this;
        }

        public Builder silencePercent(Double silencePercent) {
            message.silencePercent = silencePercent;
            return this;
        }

        public Builder packetsReceived(Long packetsReceived) {
            message.packetsReceived = packetsReceived;
            return this;
        }

        public Builder packetsForwarded(Long packetsForwarded) {
            message.packetsForwarded = packetsForwarded;
            return this;
        }

        public Builder packetsDropped(Long packetsDropped) {
            message.packetsDropped = packetsDropped;
            return this;
        }

        public Builder packetsSilenced(Long packetsSilenced) {
            message.packetsSilenced = packetsSilenced;
            return this;
        }

        public Builder error(String error) {
            message.error = error;
            return this;
        }

        public OutboundMessage build() {
            return message;
        }
    }
}
