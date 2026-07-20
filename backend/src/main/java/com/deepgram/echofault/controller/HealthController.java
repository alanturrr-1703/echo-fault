package com.deepgram.echofault.controller;

import com.deepgram.echofault.config.DeepgramProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final DeepgramProperties deepgramProperties;

    public HealthController(DeepgramProperties deepgramProperties) {
        this.deepgramProperties = deepgramProperties;
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        String apiKey = deepgramProperties.getApiKey();
        boolean configured = apiKey != null && !apiKey.isBlank();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("service", "echofault");
        body.put("deepgramConfigured", configured);
        return ResponseEntity.ok(body);
    }
}
