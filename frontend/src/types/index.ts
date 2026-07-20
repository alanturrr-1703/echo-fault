export interface ChaosSettings {
  packetLossPercent: number;
  latencyMs: number;
  silencePercent: number;
}

export interface StreamMetrics {
  packetsReceived: number;
  packetsForwarded: number;
  packetsDropped: number;
  packetsSilenced: number;
}

export interface TranscriptLine {
  text: string;
  confidence: number;
  isFinal: boolean;
  timestamp: number;
}

export type ConnectionStatus =
  | 'disconnected'
  | 'connecting'
  | 'connected'
  | 'streaming'
  | 'error';

export interface OutboundMessage {
  type: string;
  transcript?: string;
  isFinal?: boolean;
  confidence?: number;
  connectionStatus?: string;
  packetLossPercent?: number;
  latencyMs?: number;
  silencePercent?: number;
  packetsReceived?: number;
  packetsForwarded?: number;
  packetsDropped?: number;
  packetsSilenced?: number;
  error?: string;
}
