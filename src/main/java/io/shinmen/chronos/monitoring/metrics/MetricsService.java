package io.shinmen.chronos.monitoring.metrics;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetricsService {
    private final MeterRegistry registry;

    public void recordJobExecution(String jobType, boolean success, long durationMs) {
        Counter.builder("job.executions")
                .tag("type", jobType)
                .tag("status", success ? "success" : "failure")
                .register(registry)
                .increment();

        Timer.builder("job.duration")
                .tag("type", jobType)
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }
}
