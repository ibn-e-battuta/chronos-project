package io.shinmen.chronos.job.service.execution;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuartzTriggerService {
    private final Scheduler scheduler;

    public Optional<LocalDateTime> getNextFireTime(Long jobId) {
        try {
            TriggerKey triggerKey = new TriggerKey(jobId.toString());
            Trigger trigger = scheduler.getTrigger(triggerKey);
            
            if (trigger == null) {
                return Optional.empty();
            }

            Date nextFireTime = trigger.getNextFireTime();
            if (nextFireTime == null) {
                return Optional.empty();
            }

            return Optional.of(LocalDateTime.ofInstant(
                nextFireTime.toInstant(),
                ZoneId.systemDefault()
            ));
        } catch (SchedulerException e) {
            log.error("Error getting next fire time for job {}", jobId, e);
            return Optional.empty();
        }
    }

    
}
