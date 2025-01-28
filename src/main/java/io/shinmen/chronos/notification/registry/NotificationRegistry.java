package io.shinmen.chronos.notification.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.shinmen.chronos.notification.channel.base.NotificationChannel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotificationRegistry {
    private final Map<String, NotificationChannel> channels = new HashMap<>();
    private final List<NotificationChannel> availableChannels;

    public NotificationRegistry(List<NotificationChannel> notificationChannels) {
        this.availableChannels = notificationChannels;
    }

    @PostConstruct
    public void initializeChannels() {
        log.info("Initializing notification channels...");
        
        if (availableChannels.isEmpty()) {
            log.warn("No notification channels found! Please check your configuration.");
            return;
        }

        availableChannels.forEach(channel -> {
            String channelType = channel.getChannelType();
            log.info("Registering notification channel: {} ({})", 
                    channelType, channel.getClass().getSimpleName());
            
            if (channels.containsKey(channelType)) {
                log.warn("Duplicate channel type found: {}. Overwriting previous registration.", channelType);
            }
            
            channels.put(channelType, channel);
        });

        log.info("Notification channel registration complete. Registered channels: {}", 
                channels.keySet());
    }

    public NotificationChannel getChannel(String channelType) {
        NotificationChannel channel = channels.get(channelType);
        if (channel == null) {
            log.error("Requested channel type not found: {}", channelType);
            throw new IllegalArgumentException("Unsupported notification channel: " + channelType);
        }
        log.debug("Retrieved notification channel: {} ({})", 
                channelType, channel.getClass().getSimpleName());
        return channel;
    }

    public List<String> getAvailableChannels() {
        return List.copyOf(channels.keySet());
    }
}