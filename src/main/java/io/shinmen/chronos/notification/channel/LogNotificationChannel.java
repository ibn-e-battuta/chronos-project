package io.shinmen.chronos.notification.channel;

import org.springframework.stereotype.Service;

import io.shinmen.chronos.notification.channel.base.NotificationChannel;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LogNotificationChannel implements NotificationChannel {
    @Override
    public void sendNotification(String recipient, String subject, String message) {
        log.info("\nNotification sent:" +
                "\nRecipient: {}" +
                "\nSubject: {}" +
                "\nMessage: {}", 
                recipient, subject, message);
    }

    @Override
    public String getChannelType() {
        return "LOG";
    }
}