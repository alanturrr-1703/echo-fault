import type { ChaosSettings, OutboundMessage } from '../types';

const WS_URL =
  import.meta.env.VITE_WS_URL ||
  `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host}/ws/audio`;

export type MessageHandler = (message: OutboundMessage) => void;

export class EchoFaultWebSocket {
  private ws: WebSocket | null = null;
  private handler: MessageHandler | null = null;

  connect(onMessage: MessageHandler): Promise<void> {
    return new Promise((resolve, reject) => {
      this.handler = onMessage;
      this.ws = new WebSocket(WS_URL);
      this.ws.binaryType = 'arraybuffer';

      this.ws.onopen = () => resolve();
      this.ws.onerror = () => reject(new Error('WebSocket connection failed'));
      this.ws.onmessage = (event) => {
        if (typeof event.data === 'string' && this.handler) {
          try {
            this.handler(JSON.parse(event.data) as OutboundMessage);
          } catch {
            // ignore malformed messages
          }
        }
      };
    });
  }

  sendAudio(data: ArrayBuffer): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(data);
    }
  }

  sendConfig(settings: ChaosSettings): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(
        JSON.stringify({
          type: 'config',
          packetLossPercent: settings.packetLossPercent,
          latencyMs: settings.latencyMs,
          silencePercent: settings.silencePercent,
        }),
      );
    }
  }

  disconnect(): void {
    this.ws?.close();
    this.ws = null;
    this.handler = null;
  }

  isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN;
  }
}
