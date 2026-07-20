package com.deepgram.echofault.pipeline;

import com.deepgram.echofault.metrics.StreamMetrics;
import com.deepgram.echofault.model.AudioChunk;
import com.deepgram.echofault.model.ChaosConfig;
import com.deepgram.echofault.rules.AudioRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * Orchestrates audio chunks through an ordered chain of chaos rules.
 */
@Component
public class AudioPipeline {

    private final List<AudioRule> rules;

    public AudioPipeline(List<AudioRule> rules) {
        this.rules = rules;
    }

    /**
     * Processes an audio chunk through all pipeline stages.
     *
     * @param chunk   incoming audio data
     * @param config  current chaos configuration
     * @param metrics per-session metrics collector
     * @param sink    consumer for chunks that survive the pipeline
     */
    public void process(
            AudioChunk chunk,
            ChaosConfig config,
            StreamMetrics metrics,
            Consumer<AudioChunk> sink) {
        metrics.recordReceived();
        ChainedRuleContext context = new ChainedRuleContext(rules, config, metrics, sink);
        context.forward(chunk);
    }

    private static final class ChainedRuleContext implements RuleContext {

        private final List<AudioRule> rules;
        private final ChaosConfig config;
        private final StreamMetrics metrics;
        private final Consumer<AudioChunk> sink;
        private int index;

        private ChainedRuleContext(
                List<AudioRule> rules,
                ChaosConfig config,
                StreamMetrics metrics,
                Consumer<AudioChunk> sink) {
            this.rules = rules;
            this.config = config;
            this.metrics = metrics;
            this.sink = sink;
        }

        @Override
        public void forward(AudioChunk chunk) {
            if (index >= rules.size()) {
                sink.accept(chunk);
                return;
            }
            AudioRule rule = rules.get(index++);
            rule.apply(chunk, config, this);
        }

        @Override
        public void drop(String reason) {
            metrics.recordDropped();
        }

        @Override
        public void recordSilenced() {
            metrics.recordSilenced();
        }
    }
}
