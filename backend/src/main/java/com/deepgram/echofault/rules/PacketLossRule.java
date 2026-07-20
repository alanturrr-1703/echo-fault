package com.deepgram.echofault.rules;

import com.deepgram.echofault.model.AudioChunk;
import com.deepgram.echofault.model.ChaosConfig;
import com.deepgram.echofault.pipeline.RuleContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Randomly discards a configurable percentage of audio packets.
 */
@Component
public class PacketLossRule implements AudioRule {

    @Override
    public void apply(AudioChunk chunk, ChaosConfig config, RuleContext context) {
        double lossPercent = config.getPacketLossPercent();
        if (lossPercent > 0 && ThreadLocalRandom.current().nextDouble(100) < lossPercent) {
            context.drop("packet_loss");
            return;
        }
        context.forward(chunk);
    }
}
