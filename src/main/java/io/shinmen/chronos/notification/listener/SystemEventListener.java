package io.shinmen.chronos.notification.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import io.shinmen.chronos.notification.event.system.SystemAlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemEventListener {
    @Async
    @EventListener
    public void handleSystemAlert(SystemAlertEvent event) {
        log.warn("System Alert - Severity: {}, Component: {}, Message: {}", 
                event.getSeverity(), 
                event.getComponent(), 
                event.getMessage());
    }
}
