import { ChaosSlider } from '../components/ChaosSlider';
import { MetricsPanel } from '../components/MetricsPanel';
import { StatusBadge } from '../components/StatusBadge';
import { TranscriptPanel } from '../components/TranscriptPanel';
import { useEchoFault } from '../hooks/useEchoFault';

export function Dashboard() {
  const {
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
  } = useEchoFault();

  return (
    <div className="min-h-screen">
      <header className="border-b border-[var(--color-echo-border)] bg-[var(--color-echo-surface)]">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
          <div>
            <h1 className="text-2xl font-bold tracking-tight">
              <span className="text-[var(--color-echo-accent)]">Echo</span>Fault
            </h1>
            <p className="text-xs text-gray-500">Inject • Observe • Measure • Improve</p>
          </div>
          <StatusBadge status={connectionStatus} />
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-6 py-8">
        {error && (
          <div className="mb-6 rounded-lg border border-red-800 bg-red-900/20 px-4 py-3 text-sm text-red-300">
            {error}
          </div>
        )}

        <div className="mb-6 flex flex-wrap gap-3">
          {!isRecording ? (
            <button
              onClick={startRecording}
              className="rounded-lg bg-[var(--color-echo-accent)] px-6 py-2.5 text-sm font-semibold text-black transition hover:bg-[var(--color-echo-accent-dim)]"
            >
              Start Recording
            </button>
          ) : (
            <button
              onClick={stopRecording}
              className="rounded-lg bg-red-600 px-6 py-2.5 text-sm font-semibold text-white transition hover:bg-red-700"
            >
              Stop Recording
            </button>
          )}
          <button
            onClick={clearTranscripts}
            className="rounded-lg border border-[var(--color-echo-border)] px-6 py-2.5 text-sm text-gray-300 transition hover:bg-[var(--color-echo-surface)]"
          >
            Clear Transcript
          </button>
        </div>

        <div className="grid gap-6 lg:grid-cols-3">
          <div className="lg:col-span-2">
            <div className="h-[500px]">
              <TranscriptPanel transcripts={transcripts} interimText={interimText} />
            </div>
          </div>

          <div className="space-y-6">
            <div className="rounded-xl border border-[var(--color-echo-border)] bg-[var(--color-echo-surface)] p-5">
              <h2 className="mb-5 text-sm font-medium text-gray-300">Chaos Controls</h2>
              <div className="space-y-6">
                <ChaosSlider
                  label="Packet Loss"
                  value={settings.packetLossPercent}
                  min={0}
                  max={50}
                  step={1}
                  unit="%"
                  color="#f43f5e"
                  onChange={(v) => updateSettings({ packetLossPercent: v })}
                />
                <ChaosSlider
                  label="Latency"
                  value={settings.latencyMs}
                  min={0}
                  max={2000}
                  step={50}
                  unit="ms"
                  color="#f59e0b"
                  onChange={(v) => updateSettings({ latencyMs: v })}
                />
                <ChaosSlider
                  label="Silence Injection"
                  value={settings.silencePercent}
                  min={0}
                  max={50}
                  step={1}
                  unit="%"
                  color="#8b5cf6"
                  onChange={(v) => updateSettings({ silencePercent: v })}
                />
              </div>
            </div>

            <MetricsPanel
              metrics={metrics}
              packetLossPercent={settings.packetLossPercent}
              latencyMs={settings.latencyMs}
              silencePercent={settings.silencePercent}
              confidence={confidence}
            />
          </div>
        </div>
      </main>

      <footer className="mt-12 border-t border-[var(--color-echo-border)] py-6 text-center text-xs text-gray-600">
        EchoFault — Chaos Engineering for Voice AI
      </footer>
    </div>
  );
}
