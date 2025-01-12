package io.shinmen.chronos.notification.channel;

import org.springframework.stereotype.Service;

import io.shinmen.chronos.notification.channel.base.NotificationChannel;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConsoleNotificationChannel implements NotificationChannel {
    @Override
    public void sendNotification(String recipient, String subject, String message) {
        System.out.println("\n=== NOTIFICATION ===");
        System.out.println("Recipient: " + recipient);
        System.out.println("Subject: " + subject);
        System.out.println("Message: " + message);
        System.out.println("==================\n");
    }

    @Override
    public String getChannelType() {
        return "CONSOLE";
    }
}
