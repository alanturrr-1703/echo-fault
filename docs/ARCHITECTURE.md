# EchoFault Architecture

## Overview

EchoFault is a WebSocket proxy that sits between a browser microphone and the Deepgram streaming API. Every audio packet passes through a configurable chaos pipeline before being forwarded.

## Data Flow

```
┌─────────────┐     WebSocket (binary)      ┌──────────────────┐
│   Browser   │ ──────────────────────────► │  Spring Boot     │
│  Microphone │                             │  WebSocket Proxy │
└─────────────┘                             └────────┬─────────┘
       ▲                                             │
       │                                             ▼
       │                                    ┌──────────────────┐
       │         WebSocket (JSON)           │  Audio Pipeline  │
       └────────────────────────────────────  │                  │
                                             │  PacketLossRule  │
                                             │       ↓          │
                                             │  LatencyRule     │
                                             │       ↓          │
                                             │  SilenceRule     │
                                             └────────┬─────────┘
                                                      │
                                                      ▼
                                             ┌──────────────────┐
                                             │  Deepgram API    │
                                             │  (Streaming STT) │
                                             └────────┬─────────┘
                                                      │
                                                      ▼
                                             Live Transcripts
```

## Backend Components

| Package | Responsibility |
|---------|---------------|
| `websocket` | Browser WebSocket handler, session management |
| `pipeline` | Orchestrates rule chain execution |
| `rules` | Individual chaos injection stages |
| `deepgram` | Outbound WebSocket client to Deepgram |
| `metrics` | Per-session packet counters |
| `model` | Data transfer objects |
| `config` | Spring configuration and properties |

## Frontend Components

| Directory | Responsibility |
|-----------|---------------|
| `hooks` | `useEchoFault` — central state management |
| `services` | WebSocket client and audio capture |
| `components` | Dashboard UI building blocks |
| `pages` | Main dashboard layout |

## Chaos Rule Design

All rules implement the `AudioRule` interface and are chained via `RuleContext`:

```java
public interface AudioRule {
    void apply(AudioChunk chunk, ChaosConfig config, RuleContext context);
}
```

Rules call `context.forward(chunk)` to pass to the next stage, or `context.drop(reason)` to discard the packet. The `LatencyRule` schedules deferred forwarding via a `ScheduledExecutorService`.

## WebSocket Protocol

### Browser → Backend (binary)
Raw PCM audio: linear16, 16 kHz, mono.

### Browser → Backend (JSON)
```json
{
  "type": "config",
  "packetLossPercent": 10,
  "latencyMs": 250,
  "silencePercent": 5
}
```

### Backend → Browser (JSON)
Transcript, metrics, status, and error messages via `OutboundMessage`.
