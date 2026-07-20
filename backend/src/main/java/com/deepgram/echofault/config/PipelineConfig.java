package com.deepgram.echofault.config;

import com.deepgram.echofault.rules.LatencyRule;
import com.deepgram.echofault.rules.PacketLossRule;
import com.deepgram.echofault.rules.SilenceRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Defines the ordered chaos rule pipeline: packet loss → latency → silence.
 */
@Configuration
public class PipelineConfig {

    @Bean
    public List<com.deepgram.echofault.rules.AudioRule> audioRules(
            PacketLossRule packetLossRule,
            LatencyRule latencyRule,
            SilenceRule silenceRule) {
        return List.of(packetLossRule, latencyRule, silenceRule);
    }
}
