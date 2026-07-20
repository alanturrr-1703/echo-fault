import { useEffect, useRef } from 'react';
import type { TranscriptLine } from '../types';

interface TranscriptPanelProps {
  transcripts: TranscriptLine[];
  interimText: string;
}

export function TranscriptPanel({ transcripts, interimText }: TranscriptPanelProps) {
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [transcripts, interimText]);

  return (
    <div className="flex h-full flex-col rounded-xl border border-[var(--color-echo-border)] bg-[var(--color-echo-surface)]">
      <div className="border-b border-[var(--color-echo-border)] px-5 py-3">
        <h2 className="text-sm font-medium text-gray-300">Live Transcript</h2>
      </div>

      <div className="flex-1 overflow-y-auto p-5">
        {transcripts.length === 0 && !interimText && (
          <p className="text-center text-sm text-gray-500">
            Start recording and speak to see live transcription...
          </p>
        )}

        <div className="space-y-3">
          {transcripts.map((line, i) => (
            <div key={`${line.timestamp}-${i}`} className="group">
              <p className="text-base leading-relaxed text-gray-100">{line.text}</p>
              <p className="mt-1 text-xs text-gray-500">
                confidence: {(line.confidence * 100).toFixed(1)}%
              </p>
            </div>
          ))}

          {interimText && (
            <p className="text-base italic leading-relaxed text-gray-400">{interimText}</p>
          )}
        </div>
        <div ref={bottomRef} />
      </div>
    </div>
  );
}
