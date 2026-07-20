package com.deepgram.echofault.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Logs a clear startup warning when the Deepgram API key is missing.
 */
@Component
public class DeepgramConfigValidator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DeepgramConfigValidator.class);

    private final DeepgramProperties properties;

    public DeepgramConfigValidator(DeepgramProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("""
                    DEEPGRAM_API_KEY is not set. Transcription will fail until configured.
                    Set it via environment variable or a .env file in the project root:
                      export DEEPGRAM_API_KEY=your_key_here
                    Get a key at https://console.deepgram.com/
                    """);
        } else {
            log.info("Deepgram API key configured ({} chars)", apiKey.length());
        }
    }
}
