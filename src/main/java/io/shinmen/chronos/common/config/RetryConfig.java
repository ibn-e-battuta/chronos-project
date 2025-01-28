package io.shinmen.chronos.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "job.retry")
public class RetryConfig {
    private int maxAttempts = 3;
    private long initialDelay = 1000; // 1 second
    private double multiplier = 2.0;
    private long maxDelay = 60000; // 1 minute
}
