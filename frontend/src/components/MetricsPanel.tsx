import type { StreamMetrics } from '../types';

interface MetricsPanelProps {
  metrics: StreamMetrics;
  packetLossPercent: number;
  latencyMs: number;
  silencePercent: number;
  confidence: number | null;
}

export function MetricsPanel({
  metrics,
  packetLossPercent,
  latencyMs,
  silencePercent,
  confidence,
}: MetricsPanelProps) {
  const cards = [
    { label: 'Packet Loss', value: `${packetLossPercent}%`, color: '#f43f5e' },
    { label: 'Latency', value: `${latencyMs} ms`, color: '#f59e0b' },
    { label: 'Silence', value: `${silencePercent}%`, color: '#8b5cf6' },
    {
      label: 'Confidence',
      value: confidence !== null ? `${(confidence * 100).toFixed(1)}%` : '—',
      color: '#13ef93',
    },
  ];

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-2 gap-3">
        {cards.map((card) => (
          <div
            key={card.label}
            className="rounded-lg border border-[var(--color-echo-border)] bg-[var(--color-echo-surface)] p-4"
          >
            <p className="text-xs text-gray-500">{card.label}</p>
            <p className="mt-1 text-xl font-semibold" style={{ color: card.color }}>
              {card.value}
            </p>
          </div>
        ))}
      </div>

      <div className="rounded-lg border border-[var(--color-echo-border)] bg-[var(--color-echo-surface)] p-4">
        <p className="mb-3 text-xs font-medium uppercase tracking-wider text-gray-500">
          Pipeline Stats
        </p>
        <div className="grid grid-cols-2 gap-2 text-sm">
          <Stat label="Received" value={metrics.packetsReceived} />
          <Stat label="Forwarded" value={metrics.packetsForwarded} />
          <Stat label="Dropped" value={metrics.packetsDropped} />
          <Stat label="Silenced" value={metrics.packetsSilenced} />
        </div>
      </div>
    </div>
  );
}

function Stat({ label, value }: { label: string; value: number }) {
  return (
    <div className="flex justify-between rounded bg-black/20 px-3 py-2">
      <span className="text-gray-400">{label}</span>
      <span className="font-mono text-gray-200">{value.toLocaleString()}</span>
    </div>
  );
}
