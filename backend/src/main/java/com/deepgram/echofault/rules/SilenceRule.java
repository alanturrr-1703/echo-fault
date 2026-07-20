package com.deepgram.echofault.rules;

import com.deepgram.echofault.model.AudioChunk;
import com.deepgram.echofault.model.ChaosConfig;
import com.deepgram.echofault.pipeline.RuleContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Replaces a configurable percentage of packets with silence (zero-filled audio).
 */
@Component
public class SilenceRule implements AudioRule {

    @Override
    public void apply(AudioChunk chunk, ChaosConfig config, RuleContext context) {
        double silencePercent = config.getSilencePercent();
        if (silencePercent > 0 && ThreadLocalRandom.current().nextDouble(100) < silencePercent) {
            context.recordSilenced();
            byte[] silent = new byte[chunk.length()];
            Arrays.fill(silent, (byte) 0);
            context.forward(chunk.withData(silent));
            return;
        }
        context.forward(chunk);
    }
}
