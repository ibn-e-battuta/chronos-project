package io.shinmen.chronos.notification.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import io.shinmen.chronos.auth.model.User;
import io.shinmen.chronos.common.enums.NotificationType;
import io.shinmen.chronos.job.model.Job;
import io.shinmen.chronos.notification.channel.base.NotificationChannel;
import io.shinmen.chronos.notification.model.NotificationPreference;
import io.shinmen.chronos.notification.registry.NotificationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRegistry notificationRegistry;

    public void sendNotification(Job job, NotificationType type, String subject, String message) {
        User user = job.getUser();
        List<NotificationPreference> preferences = user.getNotificationPreferences().stream()
                .filter(pref -> pref.getType() == type && pref.isEnabled())
                .collect(Collectors.toList());

        // If no specific preferences, use default channels
        if (preferences.isEmpty()) {
            // Always log by default
            try {
                NotificationChannel logChannel = notificationRegistry.getChannel("LOG");
                logChannel.sendNotification(user.getEmail(), subject, message);
            } catch (Exception e) {
                log.error("Failed to send log notification", e);
            }

            // Use email as default channel
            try {
                NotificationChannel emailChannel = notificationRegistry.getChannel("EMAIL");
                emailChannel.sendNotification(user.getEmail(), subject, message);
            } catch (Exception e) {
                log.error("Failed to send email notification", e);
            }
        } else {
            // Send to all configured channels
            for (NotificationPreference pref : preferences) {
                try {
                    NotificationChannel channel = notificationRegistry.getChannel(pref.getChannelType());
                    channel.sendNotification(pref.getDestination(), subject, message);
                } catch (Exception e) {
                    log.error("Failed to send notification via channel {} for job {}",
                            pref.getChannelType(), job.getId(), e);
                }
            }
        }
    }
}