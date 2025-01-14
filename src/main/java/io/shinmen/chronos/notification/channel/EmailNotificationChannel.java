package io.shinmen.chronos.notification.channel;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import io.shinmen.chronos.notification.channel.base.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationChannel implements NotificationChannel {
    
    private final JavaMailSender mailSender;

    @Override
    public void sendNotification(String recipient, String subject, String message) {
        SimpleMailMessage emailMessage = new SimpleMailMessage();
        emailMessage.setTo(recipient);
        emailMessage.setSubject(subject);
        emailMessage.setText(message);
        log.info("Sending email: {}", message);
    }

    @Override
    public String getChannelType() {
        return "EMAIL";
    }
}
