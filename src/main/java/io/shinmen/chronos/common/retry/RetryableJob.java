package io.shinmen.chronos.common.retry;

import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import io.shinmen.chronos.common.config.RetryConfig;
import io.shinmen.chronos.common.enums.JobStatus;
import io.shinmen.chronos.job.model.Job;
import io.shinmen.chronos.notification.event.job.JobFailureEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryableJob {
    private final ApplicationEventPublisher eventPublisher;
    private final RetryConfig retryConfig;

    public void execute(Job job, JobExecutionContext context, JobFunction jobFunction) throws Exception {
        int attempt = job.getRetryCount();
        long delay = calculateDelay(attempt);

        try {
            jobFunction.execute();
            job.setStatus(JobStatus.COMPLETED);
            job.setRetryCount(0);
        } catch (Exception e) {
            handleFailure(job, context, e, attempt, delay);
            throw e;
        }
    }

    private void handleFailure(Job job, JobExecutionContext context, Exception e, int attempt, long delay) {
        job.setRetryCount(attempt + 1);

        if (attempt >= retryConfig.getMaxAttempts() - 1) {
            job.setStatus(JobStatus.FAILED);
            eventPublisher.publishEvent(new JobFailureEvent(this, job, e.getMessage()));
            log.error("Job {} failed permanently after {} attempts. Error: {}",
                    job.getId(), attempt + 1, e.getMessage());
            return;
        }

        job.setStatus(JobStatus.SCHEDULED);
        rescheduleJob(job, context, delay);

        log.warn("Job {} failed attempt {}. Retrying in {} ms. Error: {}",
                job.getId(), attempt + 1, delay, e.getMessage());
    }

    private long calculateDelay(int attempt) {
        long delay = (long) (retryConfig.getInitialDelay() *
                Math.pow(retryConfig.getMultiplier(), attempt));
        return Math.min(delay, retryConfig.getMaxDelay());
    }

    private void rescheduleJob(Job job, JobExecutionContext context, long delay) {
        try {
            context.getScheduler().triggerJob(
                    context.getJobDetail().getKey(),
                    context.getMergedJobDataMap()
            );
        } catch (SchedulerException e) {
            log.error("Failed to reschedule job {}", job.getId(), e);
            throw new RuntimeException("Failed to reschedule job", e);
        }
    }
}
