package com.deepgram.echofault.pipeline;

import com.deepgram.echofault.metrics.StreamMetrics;
import com.deepgram.echofault.model.AudioChunk;
import com.deepgram.echofault.model.ChaosConfig;
import com.deepgram.echofault.rules.LatencyRule;
import com.deepgram.echofault.rules.PacketLossRule;
import com.deepgram.echofault.rules.SilenceRule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AudioPipelineTest {

    @Test
    void passesChunksThroughWithNoChaos() {
        AudioPipeline pipeline = new AudioPipeline(List.of(
                new PacketLossRule(),
                new LatencyRule(),
                new SilenceRule()));

        ChaosConfig config = new ChaosConfig();
        StreamMetrics metrics = new StreamMetrics();
        List<byte[]> outputs = new ArrayList<>();

        byte[] input = {1, 2, 3, 4};
        pipeline.process(new AudioChunk(input), config, metrics, chunk -> outputs.add(chunk.getData()));

        assertEquals(1, outputs.size());
        assertArrayEquals(input, outputs.getFirst());
        assertEquals(1, metrics.getPacketsReceived().get());
    }

    @Test
    void silenceRuleReplacesAudioWithZeros() throws Exception {
        AudioPipeline pipeline = new AudioPipeline(List.of(
                new PacketLossRule(),
                new LatencyRule(),
                new SilenceRule()));

        ChaosConfig config = new ChaosConfig();
        config.setSilencePercent(100);
        StreamMetrics metrics = new StreamMetrics();
        List<byte[]> outputs = new ArrayList<>();

        pipeline.process(new AudioChunk(new byte[]{9, 9, 9}), config, metrics, chunk -> outputs.add(chunk.getData()));
        TimeUnit.MILLISECONDS.sleep(50);

        assertEquals(1, outputs.size());
        assertArrayEquals(new byte[]{0, 0, 0}, outputs.getFirst());
        assertEquals(1, metrics.getPacketsSilenced().get());
    }
}
