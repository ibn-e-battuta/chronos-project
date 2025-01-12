package io.shinmen.chronos.notification.channel.base;

public interface NotificationChannel {
    void sendNotification(String recipient, String subject, String message);
    String getChannelType();
}
