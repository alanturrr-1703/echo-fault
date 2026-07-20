package com.deepgram.echofault.model;

/**
 * Raw audio data passing through the chaos pipeline.
 */
public class AudioChunk {

    private final byte[] data;
    private final long timestampMs;

    public AudioChunk(byte[] data) {
        this(data, System.currentTimeMillis());
    }

    public AudioChunk(byte[] data, long timestampMs) {
        this.data = data;
        this.timestampMs = timestampMs;
    }

    public byte[] getData() {
        return data;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public int length() {
        return data.length;
    }

    public AudioChunk withData(byte[] newData) {
        return new AudioChunk(newData, timestampMs);
    }
}
