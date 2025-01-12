package io.shinmen.chronos.job.service.execution;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import io.shinmen.chronos.common.enums.JobStatus;
import io.shinmen.chronos.common.retry.JobFunction;
import io.shinmen.chronos.common.retry.RetryableJob;
import io.shinmen.chronos.job.model.Job;
import io.shinmen.chronos.job.repository.JobRepository;
import io.shinmen.chronos.job.service.JobService;
import io.shinmen.chronos.job.type.JobTypeHandler;
import io.shinmen.chronos.job.type.JobTypeRegistry;
import io.shinmen.chronos.monitoring.metrics.MetricsService;
import io.shinmen.chronos.notification.event.job.JobFailureEvent;
import io.shinmen.chronos.notification.event.job.JobStartedEvent;
import io.shinmen.chronos.notification.event.job.JobSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class QuartzJobExecutor extends QuartzJobBean {
    private final JobService jobService;
    private final JobRepository jobRepository;
    private final JobTypeRegistry jobTypeRegistry;
    private final RetryableJob retryableJob;
    private final MetricsService metricsService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    protected void executeInternal(@NonNull JobExecutionContext context) throws JobExecutionException {
        Long jobId = context.getJobDetail().getJobDataMap().getLong("jobId");
        log.info("Starting job execution for job ID: {}", jobId);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobExecutionException("Job not found"));

        String executionId = UUID.randomUUID().toString();
        eventPublisher.publishEvent(new JobStartedEvent(this, job, executionId));

        boolean success = false;
        LocalDateTime startTime = LocalDateTime.now();
        String errorMessage = null;

        try {
            job.setStatus(JobStatus.RUNNING);
            jobRepository.save(job);

            JobFunction jobFunction = () -> {
                JobTypeHandler handler = jobTypeRegistry.getHandler(job.getJobType());
                handler.execute(job);
            };

            retryableJob.execute(job, context, jobFunction);
            jobRepository.save(job);
            success = true;
            eventPublisher.publishEvent(new JobSuccessEvent(this, job, "Job completed successfully"));
        } catch (Exception e) {
            errorMessage = e.getMessage();
            eventPublisher.publishEvent(new JobFailureEvent(this, job, errorMessage));
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            long duration = Duration.between(startTime, endTime).toMillis();
            jobService.recordJobExecution(jobId, startTime, endTime, errorMessage);
            metricsService.recordJobExecution(job.getJobType().toString(), success, duration);
        }
    }
}