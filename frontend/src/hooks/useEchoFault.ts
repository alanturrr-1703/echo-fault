import { useCallback, useEffect, useRef, useState } from 'react';
import { createAudioCapture } from '../services/audio';
import { EchoFaultWebSocket } from '../services/websocket';
import type {
  ChaosSettings,
  ConnectionStatus,
  OutboundMessage,
  StreamMetrics,
  TranscriptLine,
} from '../types';

const DEFAULT_SETTINGS: ChaosSettings = {
  packetLossPercent: 0,
  latencyMs: 0,
  silencePercent: 0,
};

const DEFAULT_METRICS: StreamMetrics = {
  packetsReceived: 0,
  packetsForwarded: 0,
  packetsDropped: 0,
  packetsSilenced: 0,
};

export function useEchoFault() {
  const [isRecording, setIsRecording] = useState(false);
  const [connectionStatus, setConnectionStatus] =
    useState<ConnectionStatus>('disconnected');
  const [settings, setSettings] = useState<ChaosSettings>(DEFAULT_SETTINGS);
  const [metrics, setMetrics] = useState<StreamMetrics>(DEFAULT_METRICS);
  const [transcripts, setTranscripts] = useState<TranscriptLine[]>([]);
  const [interimText, setInterimText] = useState('');
  const [confidence, setConfidence] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  const wsRef = useRef<EchoFaultWebSocket | null>(null);
  const captureRef = useRef<ReturnType<typeof createAudioCapture> | null>(null);

  const handleMessage = useCallback((message: OutboundMessage) => {
    switch (message.type) {
      case 'transcript':
        if (message.transcript) {
          setConfidence(message.confidence ?? null);
          if (message.isFinal) {
            setTranscripts((prev) => [
              ...prev,
              {
                text: message.transcript!,
                confidence: message.confidence ?? 0,
                isFinal: true,
                timestamp: Date.now(),
              },
            ]);
            setInterimText('');
          } else {
            setInterimText(message.transcript);
          }
        }
        break;
      case 'metrics':
        setMetrics({
          packetsReceived: message.packetsReceived ?? 0,
          packetsForwarded: message.packetsForwarded ?? 0,
          packetsDropped: message.packetsDropped ?? 0,
          packetsSilenced: message.packetsSilenced ?? 0,
        });
        if (message.connectionStatus) {
          setConnectionStatus(message.connectionStatus as ConnectionStatus);
        }
        break;
      case 'status':
        if (message.connectionStatus) {
          setConnectionStatus(message.connectionStatus as ConnectionStatus);
        }
        break;
      case 'error':
        setError(message.error ?? 'Unknown error');
        setConnectionStatus('error');
        break;
    }
  }, []);

  const startRecording = useCallback(async () => {
    setError(null);
    setConnectionStatus('connecting');

    const ws = new EchoFaultWebSocket();
    wsRef.current = ws;

    try {
      await ws.connect(handleMessage);
      ws.sendConfig(settings);

      const capture = createAudioCapture((data) => ws.sendAudio(data));
      captureRef.current = capture;
      await capture.start();

      setIsRecording(true);
      setConnectionStatus('streaming');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to start');
      setConnectionStatus('error');
      ws.disconnect();
    }
  }, [handleMessage, settings]);

  const stopRecording = useCallback(() => {
    captureRef.current?.stop();
    wsRef.current?.disconnect();
    captureRef.current = null;
    wsRef.current = null;
    setIsRecording(false);
    setConnectionStatus('disconnected');
    setInterimText('');
  }, []);

  const updateSettings = useCallback(
    (partial: Partial<ChaosSettings>) => {
      setSettings((prev) => {
        const next = { ...prev, ...partial };
        wsRef.current?.sendConfig(next);
        return next;
      });
    },
    [],
  );

  const clearTranscripts = useCallback(() => {
    setTranscripts([]);
    setInterimText('');
    setConfidence(null);
  }, []);

  useEffect(() => {
    return () => {
      captureRef.current?.stop();
      wsRef.current?.disconnect();
    };
  }, []);

  return {
    isRecording,
    connectionStatus,
    settings,
    metrics,
    transcripts,
    interimText,
    confidence,
    error,
    startRecording,
    stopRecording,
    updateSettings,
    clearTranscripts,
  };
}
