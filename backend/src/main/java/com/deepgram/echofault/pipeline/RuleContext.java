package com.deepgram.echofault.pipeline;

import com.deepgram.echofault.model.AudioChunk;
import com.deepgram.echofault.model.ChaosConfig;

/**
 * Context passed to each rule in the pipeline chain.
 */
public interface RuleContext {

    void forward(AudioChunk chunk);

    void drop(String reason);

    void recordSilenced();
}
