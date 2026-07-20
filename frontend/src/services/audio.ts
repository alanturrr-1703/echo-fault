const SAMPLE_RATE = 16000;
const BUFFER_SIZE = 4096;

export interface AudioCapture {
  start: () => Promise<void>;
  stop: () => void;
}

export function createAudioCapture(
  onAudioData: (data: ArrayBuffer) => void,
): AudioCapture {
  let audioContext: AudioContext | null = null;
  let mediaStream: MediaStream | null = null;
  let processor: ScriptProcessorNode | null = null;
  let source: MediaStreamAudioSourceNode | null = null;

  async function start() {
    mediaStream = await navigator.mediaDevices.getUserMedia({
      audio: {
        channelCount: 1,
        echoCancellation: true,
        noiseSuppression: true,
        autoGainControl: true,
      },
    });

    audioContext = new AudioContext({ sampleRate: SAMPLE_RATE });
    source = audioContext.createMediaStreamSource(mediaStream);
    processor = audioContext.createScriptProcessor(BUFFER_SIZE, 1, 1);

    processor.onaudioprocess = (event) => {
      const input = event.inputBuffer.getChannelData(0);
      const pcm = floatTo16BitPCM(input);
      onAudioData(pcm.buffer.slice(pcm.byteOffset, pcm.byteOffset + pcm.byteLength) as ArrayBuffer);
    };

    source.connect(processor);
    processor.connect(audioContext.destination);
  }

  function stop() {
    processor?.disconnect();
    source?.disconnect();
    mediaStream?.getTracks().forEach((track) => track.stop());
    audioContext?.close();

    processor = null;
    source = null;
    mediaStream = null;
    audioContext = null;
  }

  return { start, stop };
}

function floatTo16BitPCM(input: Float32Array): Int16Array {
  const output = new Int16Array(input.length);
  for (let i = 0; i < input.length; i++) {
    const s = Math.max(-1, Math.min(1, input[i]));
    output[i] = s < 0 ? s * 0x8000 : s * 0x7fff;
  }
  return output;
}
