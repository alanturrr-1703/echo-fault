package com.deepgram.echofault.rules;

import com.deepgram.echofault.model.AudioChunk;
import com.deepgram.echofault.model.ChaosConfig;
import com.deepgram.echofault.pipeline.RuleContext;

/**
 * A single stage in the audio chaos pipeline.
 */
public interface AudioRule {

    void apply(AudioChunk chunk, ChaosConfig config, RuleContext context);
}
