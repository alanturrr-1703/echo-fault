package com.deepgram.echofault.websocket;

import com.deepgram.echofault.deepgram.DeepgramClient;
import com.deepgram.echofault.metrics.StreamMetrics;
import com.deepgram.echofault.model.AudioChunk;
import com.deepgram.echofault.model.ChaosConfig;
import com.deepgram.echofault.model.InboundMessage;
import com.deepgram.echofault.model.OutboundMessage;
import com.deepgram.echofault.pipeline.AudioPipeline;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Proxies browser audio through the chaos pipeline to Deepgram and streams transcripts back.
 */
@Component
public class EchoFaultWebSocketHandler extends AbstractWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(EchoFaultWebSocketHandler.class);

    private final AudioPipeline pipeline;
    private final DeepgramClient deepgramClient;
    private final ObjectMapper objectMapper;

    private final Map<String, SessionState> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService metricsScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "metrics-broadcast");
        t.setDaemon(true);
        return t;
    });

    public EchoFaultWebSocketHandler(
            AudioPipeline pipeline,
            DeepgramClient deepgramClient,
            ObjectMapper objectMapper) {
        this.pipeline = pipeline;
        this.deepgramClient = deepgramClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Client connected: {}", session.getId());

        SessionState state = new SessionState();
        sessions.put(session.getId(), state);

        try {
            DeepgramClient.DeepgramSession dgSession = deepgramClient.connect(
                    result -> sendTranscript(session, result),
                    error -> sendError(session, error));

            state.deepgramSession = dgSession;
            state.metricsTask = metricsScheduler.scheduleAtFixedRate(
                    () -> broadcastMetrics(session, state),
                    0, 500, TimeUnit.MILLISECONDS);

            sendStatus(session, "connected");
        } catch (Exception e) {
            log.error("Failed to establish Deepgram session", e);
            sendError(session, e.getMessage());
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        SessionState state = sessions.get(session.getId());
        if (state == null || state.deepgramSession == null) {
            return;
        }

        ByteBuffer buffer = message.getPayload();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        AudioChunk chunk = new AudioChunk(data);
        pipeline.process(chunk, state.config, state.metrics, processed -> {
            state.metrics.recordForwarded();
            state.deepgramSession.sendAudio(processed.getData());
        });
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        SessionState state = sessions.get(session.getId());
        if (state == null) {
            return;
        }

        try {
            InboundMessage inbound = objectMapper.readValue(message.getPayload(), InboundMessage.class);
            if ("config".equals(inbound.getType())) {
                applyConfig(state, inbound);
                broadcastMetrics(session, state);
            }
        } catch (Exception e) {
            log.warn("Invalid control message: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("Client disconnected: {} ({})", session.getId(), status);
        SessionState state = sessions.remove(session.getId());
        if (state != null) {
            if (state.metricsTask != null) {
                state.metricsTask.cancel(false);
            }
            if (state.deepgramSession != null) {
                state.deepgramSession.close();
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Transport error for session {}", session.getId(), exception);
        sendError(session, exception.getMessage());
    }

    private void applyConfig(SessionState state, InboundMessage inbound) {
        if (inbound.getPacketLossPercent() != null) {
            state.config.setPacketLossPercent(clamp(inbound.getPacketLossPercent(), 0, 50));
        }
        if (inbound.getLatencyMs() != null) {
            state.config.setLatencyMs(clamp(inbound.getLatencyMs(), 0, 2000));
        }
        if (inbound.getSilencePercent() != null) {
            state.config.setSilencePercent(clamp(inbound.getSilencePercent(), 0, 50));
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    private void sendTranscript(WebSocketSession session, DeepgramClient.TranscriptResult result) {
        OutboundMessage msg = OutboundMessage.builder()
                .type("transcript")
                .transcript(result.getTranscript())
                .isFinal(result.isFinal())
                .confidence(result.getConfidence())
                .build();
        sendJson(session, msg);
    }

    private void sendStatus(WebSocketSession session, String status) {
        OutboundMessage msg = OutboundMessage.builder()
                .type("status")
                .connectionStatus(status)
                .build();
        sendJson(session, msg);
    }

    private void sendError(WebSocketSession session, String error) {
        OutboundMessage msg = OutboundMessage.builder()
                .type("error")
                .error(error)
                .connectionStatus("error")
                .build();
        sendJson(session, msg);
    }

    private void broadcastMetrics(WebSocketSession session, SessionState state) {
        if (!session.isOpen()) {
            return;
        }
        OutboundMessage msg = OutboundMessage.builder()
                .type("metrics")
                .packetLossPercent(state.config.getPacketLossPercent())
                .latencyMs(state.config.getLatencyMs())
                .silencePercent(state.config.getSilencePercent())
                .packetsReceived(state.metrics.getPacketsReceived().get())
                .packetsForwarded(state.metrics.getPacketsForwarded().get())
                .packetsDropped(state.metrics.getPacketsDropped().get())
                .packetsSilenced(state.metrics.getPacketsSilenced().get())
                .connectionStatus(state.deepgramSession != null && state.deepgramSession.isOpen()
                        ? "streaming" : "connecting")
                .build();
        sendJson(session, msg);
    }

    private void sendJson(WebSocketSession session, OutboundMessage msg) {
        if (!session.isOpen()) {
            return;
        }
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
            }
        } catch (IOException e) {
            log.warn("Failed to send message to client: {}", e.getMessage());
        }
    }

    private static final class SessionState {
        final ChaosConfig config = new ChaosConfig();
        final StreamMetrics metrics = new StreamMetrics();
        DeepgramClient.DeepgramSession deepgramSession;
        ScheduledFuture<?> metricsTask;
    }
}
