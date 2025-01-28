package io.shinmen.chronos.monitoring.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import io.shinmen.chronos.notification.event.system.SystemAlertEvent;
import io.shinmen.chronos.notification.event.system.SystemAlertEvent.SystemAlertSeverity;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JobSystemHealthIndicator implements HealthIndicator {
    private final ApplicationEventPublisher eventPublisher;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Health health() {
        try {
            jdbcTemplate.queryForObject("SELECT 1 FROM jobs LIMIT 1", Integer.class);
            return Health.up()
                    .withDetail("database", "accessible")
                    .build();
        } catch (Exception e) {
            eventPublisher.publishEvent(new SystemAlertEvent(
                    this,
                    "Database connectivity issue: " + e.getMessage(),
                    SystemAlertSeverity.CRITICAL,
                    "Database"));

            return Health.down()
                    .withDetail("database", "inaccessible")
                    .withException(e)
                    .build();
        }
    }
}
