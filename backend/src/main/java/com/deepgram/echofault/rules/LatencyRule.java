package com.deepgram.echofault.rules;

import com.deepgram.echofault.model.AudioChunk;
import com.deepgram.echofault.model.ChaosConfig;
import com.deepgram.echofault.pipeline.RuleContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Delays outgoing audio packets by a configurable amount.
 */
@Component
public class LatencyRule implements AudioRule {

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(2, r -> {
                Thread t = new Thread(r, "latency-rule");
                t.setDaemon(true);
                return t;
            });

    @Override
    public void apply(AudioChunk chunk, ChaosConfig config, RuleContext context) {
        long latencyMs = config.getLatencyMs();
        if (latencyMs <= 0) {
            context.forward(chunk);
            return;
        }
        scheduler.schedule(() -> context.forward(chunk), latencyMs, TimeUnit.MILLISECONDS);
    }
}
