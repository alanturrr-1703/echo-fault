package com.deepgram.echofault.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "echofault.deepgram")
public class DeepgramProperties {

    private String apiKey = "";
    private String model = "nova-2";
    private int sampleRate = 16000;
    private String encoding = "linear16";
    private int channels = 1;
    private boolean interimResults = true;
    private boolean punctuate = true;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public boolean isInterimResults() {
        return interimResults;
    }

    public void setInterimResults(boolean interimResults) {
        this.interimResults = interimResults;
    }

    public boolean isPunctuate() {
        return punctuate;
    }

    public void setPunctuate(boolean punctuate) {
        this.punctuate = punctuate;
    }
}
