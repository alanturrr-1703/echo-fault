package com.deepgram.echofault.deepgram;

import com.deepgram.echofault.config.DeepgramProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

/**
 * Manages a streaming WebSocket connection to the Deepgram listen API.
 */
@Component
public class DeepgramClient {

    private static final Logger log = LoggerFactory.getLogger(DeepgramClient.class);

    private final DeepgramProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public DeepgramClient(DeepgramProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Opens a new streaming session to Deepgram.
     *
     * @param onTranscript callback for transcript events
     * @param onError      callback for connection errors
     * @return session handle for sending audio and closing
     */
    public DeepgramSession connect(
            Consumer<TranscriptResult> onTranscript,
            Consumer<String> onError) {

        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new IllegalStateException(
                    "DEEPGRAM_API_KEY is not configured. Set the environment variable or add it to a .env file.");
        }

        String url = buildListenUrl();
        DeepgramSession session = new DeepgramSession();

        WebSocket.Listener listener = new WebSocket.Listener() {
            private final StringBuilder messageBuffer = new StringBuilder();

            @Override
            public void onOpen(WebSocket webSocket) {
                log.info("Connected to Deepgram");
                session.setWebSocket(webSocket);
                webSocket.request(1);
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                messageBuffer.append(data);
                if (last) {
                    parseTranscript(messageBuffer.toString(), onTranscript);
                    messageBuffer.setLength(0);
                }
                webSocket.request(1);
                return null;
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                log.error("Deepgram WebSocket error", error);
                onError.accept(error.getMessage());
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                log.info("Deepgram connection closed: {} {}", statusCode, reason);
                session.markClosed();
                return null;
            }
        };

        CompletableFuture<WebSocket> future = httpClient.newWebSocketBuilder()
                .header("Authorization", "Token " + properties.getApiKey())
                .buildAsync(URI.create(url), listener);

        future.exceptionally(ex -> {
            String message = toErrorMessage(ex);
            log.error("Failed to connect to Deepgram: {}", message);
            onError.accept(message);
            session.markClosed();
            return null;
        });

        return session;
    }

    private String toErrorMessage(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        String message = cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
        if (message.contains("401")) {
            return "Deepgram authentication failed (401). Verify DEEPGRAM_API_KEY is valid at https://console.deepgram.com/";
        }
        return "Deepgram connection failed: " + message;
    }

    private String buildListenUrl() {
        return "wss://api.deepgram.com/v1/listen"
                + "?encoding=" + properties.getEncoding()
                + "&sample_rate=" + properties.getSampleRate()
                + "&channels=" + properties.getChannels()
                + "&model=" + properties.getModel()
                + "&interim_results=" + properties.isInterimResults()
                + "&punctuate=" + properties.isPunctuate()
                + "&smart_format=true";
    }

    private void parseTranscript(String json, Consumer<TranscriptResult> onTranscript) {
        try {
            JsonNode root = objectMapper.readTree(json);
            String type = root.path("type").asText("");
            if (!"Results".equals(type)) {
                return;
            }

            JsonNode channel = root.path("channel");
            JsonNode alternatives = channel.path("alternatives");
            if (!alternatives.isArray() || alternatives.isEmpty()) {
                return;
            }

            JsonNode best = alternatives.get(0);
            String transcript = best.path("transcript").asText("").trim();
            if (transcript.isEmpty()) {
                return;
            }

            double confidence = best.path("confidence").asDouble(0);
            boolean isFinal = root.path("is_final").asBoolean(false);

            onTranscript.accept(new TranscriptResult(transcript, confidence, isFinal));
        } catch (Exception e) {
            log.warn("Failed to parse Deepgram response: {}", e.getMessage());
        }
    }

    public static final class TranscriptResult {
        private final String transcript;
        private final double confidence;
        private final boolean isFinal;

        public TranscriptResult(String transcript, double confidence, boolean isFinal) {
            this.transcript = transcript;
            this.confidence = confidence;
            this.isFinal = isFinal;
        }

        public String getTranscript() {
            return transcript;
        }

        public double getConfidence() {
            return confidence;
        }

        public boolean isFinal() {
            return isFinal;
        }
    }

    public static final class DeepgramSession {

        private volatile WebSocket webSocket;
        private volatile boolean closed;

        void setWebSocket(WebSocket webSocket) {
            this.webSocket = webSocket;
        }

        void markClosed() {
            this.closed = true;
        }

        public boolean isOpen() {
            return webSocket != null && !closed;
        }

        public void sendAudio(byte[] data) {
            if (webSocket != null && !closed) {
                webSocket.sendBinary(ByteBuffer.wrap(data), true);
            }
        }

        public void close() {
            if (webSocket != null && !closed) {
                closed = true;
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "session ended");
            }
        }
    }
}
