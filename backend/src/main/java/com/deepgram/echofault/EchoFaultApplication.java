package com.deepgram.echofault;

import com.deepgram.echofault.config.DeepgramProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DeepgramProperties.class)
public class EchoFaultApplication {

    public static void main(String[] args) {
        SpringApplication.run(EchoFaultApplication.class, args);
    }
}
