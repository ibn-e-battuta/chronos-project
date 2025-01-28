package io.shinmen.chronos.notification.event.system;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class SystemAlertEvent extends ApplicationEvent {
    private final String message;
    private final SystemAlertSeverity severity;
    private final String component;

    public SystemAlertEvent(Object source, String message) {
        this(source, message, SystemAlertSeverity.ERROR, "SYSTEM");
    }

    public SystemAlertEvent(Object source, String message, SystemAlertSeverity severity, String component) {
        super(source);
        this.message = message;
        this.severity = severity;
        this.component = component;
    }

    public enum SystemAlertSeverity {
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }
}
