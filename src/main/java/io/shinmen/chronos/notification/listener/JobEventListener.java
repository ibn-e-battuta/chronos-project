package io.shinmen.chronos.notification.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import io.shinmen.chronos.common.enums.NotificationType;
import io.shinmen.chronos.job.service.execution.QuartzTriggerService;
import io.shinmen.chronos.notification.event.job.JobCancelledEvent;
import io.shinmen.chronos.notification.event.job.JobFailureEvent;
import io.shinmen.chronos.notification.event.job.JobPausedEvent;
import io.shinmen.chronos.notification.event.job.JobResumedEvent;
import io.shinmen.chronos.notification.event.job.JobScheduledEvent;
import io.shinmen.chronos.notification.event.job.JobStartedEvent;
import io.shinmen.chronos.notification.event.job.JobSuccessEvent;
import io.shinmen.chronos.notification.service.NotificationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobEventListener {
    private final NotificationService notificationService;
    private final QuartzTriggerService quartzTriggerService;

    @Async
    @EventListener
    public void handleJobFailure(JobFailureEvent event) {
        try {
            notificationService.sendNotification(
                event.getJob(),
                NotificationType.JOB_FAILURE,
                "Job Failed",
                buildFailureMessage(event)
            );
        } catch (Exception e) {
            log.error("Failed to process job failure event for job: {}", event.getJob().getId(), e);
        }
    }

    @Async
    @EventListener
    public void handleJobSuccess(JobSuccessEvent event) {
        try {
            notificationService.sendNotification(
                event.getJob(),
                NotificationType.JOB_SUCCESS,
                "Job Completed Successfully",
                buildSuccessMessage(event)
            );
        } catch (Exception e) {
            log.error("Failed to process job success event for job: {}", event.getJob().getId(), e);
        }
    }

    @Async
    @EventListener
    public void handleJobStarted(JobStartedEvent event) {
        try {
            notificationService.sendNotification(
                event.getJob(),
                NotificationType.JOB_STARTED,
                "Job Started",
                buildStartedMessage(event)
            );
        } catch (Exception e) {
            log.error("Failed to process job started event for job: {}", event.getJob().getId(), e);
        }
    }

    @Async
    @EventListener
    public void handleJobPaused(JobPausedEvent event) {
        try {
            notificationService.sendNotification(
                event.getJob(),
                NotificationType.JOB_STATUS_CHANGE,
                "Job Paused",
                buildPausedMessage(event)
            );
        } catch (Exception e) {
            log.error("Failed to process job paused event for job: {}", event.getJob().getId(), e);
        }
    }

    @Async
    @EventListener
    public void handleJobResumed(JobResumedEvent event) {
        try {
            notificationService.sendNotification(
                event.getJob(),
                NotificationType.JOB_STATUS_CHANGE,
                "Job Resumed",
                buildResumedMessage(event)
            );
        } catch (Exception e) {
            log.error("Failed to process job resumed event for job: {}", event.getJob().getId(), e);
        }
    }

    @Async
    @EventListener
    public void handleJobCancelled(JobCancelledEvent event) {
        try {
            notificationService.sendNotification(
                event.getJob(),
                NotificationType.JOB_STATUS_CHANGE,
                "Job Cancelled",
                buildCancelledMessage(event)
            );
        } catch (Exception e) {
            log.error("Failed to process job cancelled event for job: {}", event.getJob().getId(), e);
        }
    }

    @Async
    @EventListener
    public void handleJobScheduled(JobScheduledEvent event) {
        try {
            notificationService.sendNotification(
                event.getJob(),
                NotificationType.JOB_SCHEDULED,
                "Job Scheduled",
                buildScheduledMessage(event)
            );
        } catch (Exception e) {
            log.error("Failed to process job scheduled event for job: {}", event.getJob().getId(), e);
        }
    }

    private String buildFailureMessage(JobFailureEvent event) {
        return String.format("""
            Job '%s' (ID: %d) has failed.
            Error: %s
            """,
            event.getJob().getName(),
            event.getJob().getId(),
            event.getError()
        );
    }

    private String buildSuccessMessage(JobSuccessEvent event) {
        return String.format("""
            Job '%s' (ID: %d) completed successfully.
            Result: %s
            """,
            event.getJob().getName(),
            event.getJob().getId(),
            event.getResult()
        );
    }

    private String buildStartedMessage(JobStartedEvent event) {
        return String.format("""
            Job '%s' (ID: %d) has started.
            Execution ID: %s
            """,
            event.getJob().getName(),
            event.getJob().getId(),
            event.getExecutionId()
        );
    }

    private String buildPausedMessage(JobPausedEvent event) {
        return String.format("""
            Job '%s' (ID: %d) has been paused.
            Current Status: %s
            """,
            event.getJob().getName(),
            event.getJob().getId(),
            event.getJob().getStatus()
        );
    }

    private String buildResumedMessage(JobResumedEvent event) {
        return String.format("""
            Job '%s' (ID: %d) has been resumed.
            Current Status: %s
            """,
            event.getJob().getName(),
            event.getJob().getId(),
            event.getJob().getStatus()
        );
    }

    private String buildCancelledMessage(JobCancelledEvent event) {
        return String.format("""
            Job '%s' (ID: %d) has been cancelled.
            Reason: %s
            """,
            event.getJob().getName(),
            event.getJob().getId(),
            event.getReason()
        );
    }

    private String buildScheduledMessage(JobScheduledEvent event) {
        String nextRunTime = quartzTriggerService.getNextFireTime(event.getJob().getId())
            .map(time -> time.toString())
            .orElse("Not scheduled");

        return String.format("""
            Job '%s' (ID: %d) has been scheduled.
            Next Run Time: %s
            """,
            event.getJob().getName(),
            event.getJob().getId(),
            nextRunTime
        );
    }
}