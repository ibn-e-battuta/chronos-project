package io.shinmen.chronos.job.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.shinmen.chronos.common.enums.JobStatus;
import io.shinmen.chronos.common.exception.JobNotFoundException;
import io.shinmen.chronos.common.exception.JobOperationException;
import io.shinmen.chronos.job.model.Job;
import io.shinmen.chronos.job.model.JobExecution;
import io.shinmen.chronos.job.repository.JobExecutionRepository;
import io.shinmen.chronos.job.repository.JobRepository;
import io.shinmen.chronos.job.service.execution.QuartzJobSchedulerService;
import io.shinmen.chronos.job.type.JobTypeHandler;
import io.shinmen.chronos.job.type.JobTypeRegistry;
import io.shinmen.chronos.notification.event.job.JobCancelledEvent;
import io.shinmen.chronos.notification.event.job.JobPausedEvent;
import io.shinmen.chronos.notification.event.job.JobResumedEvent;
import io.shinmen.chronos.notification.event.job.JobScheduledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;
    private final JobExecutionRepository jobExecutionRepository;
    private final QuartzJobSchedulerService schedulerService;
    private final JobTypeRegistry jobTypeRegistry;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Job createJob(Job job) throws JsonProcessingException {
        log.info("Creating new job: {}", job.getName());
        JobTypeHandler handler = jobTypeRegistry.getHandler(job.getJobType());
        Map<String, Object> config = objectMapper.readValue(job.getJobConfiguration(),
                new TypeReference<Map<String, Object>>() {
                });
        handler.validate(config);

        job.setStatus(JobStatus.SCHEDULED);
        Job savedJob = jobRepository.save(job);
        schedulerService.scheduleJob(savedJob);
        eventPublisher.publishEvent(new JobScheduledEvent(this, savedJob));
        return savedJob;
    }

    @Transactional
    public Job updateJob(Job job) {
        schedulerService.updateJob(job);
        return jobRepository.save(job);
    }

    @Transactional
    public void deleteJob(Long jobId) {
        schedulerService.deleteJob(jobId);
        jobRepository.deleteById(jobId);
    }

    @Transactional
    public void cancelJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));
        job.setStatus(JobStatus.CANCELLED);
        schedulerService.deleteJob(jobId);
        jobRepository.save(job);
        eventPublisher.publishEvent(new JobCancelledEvent(this, job,
                "User requested cancellation"));
    }

    @Transactional(readOnly = true)
    public List<Job> getUserJobs(Long userId) {
        return jobRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Job getJob(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
    }

    @Transactional
    public JobExecution recordJobExecution(Long jobId, LocalDateTime startTime, LocalDateTime endTime,
            String errorMessage) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        JobExecution execution = new JobExecution();
        execution.setJob(job);
        execution.setStartTime(startTime);
        execution.setEndTime(endTime);
        execution.setErrorMessage(errorMessage);

        return jobExecutionRepository.save(execution);
    }

    @Transactional
    public Job pauseJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));

        if (job.getStatus() != JobStatus.SCHEDULED && job.getStatus() != JobStatus.RUNNING) {
            throw new JobOperationException("Can only pause scheduled or running jobs");
        }

        job.setStatus(JobStatus.PAUSED);
        job.setPaused(true);
        schedulerService.pauseJob(jobId);
        Job savedJob = jobRepository.save(job);
        eventPublisher.publishEvent(new JobPausedEvent(this, savedJob));
        return savedJob;
    }

    @Transactional
    public Job resumeJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException("Job not found"));

        if (job.getStatus() != JobStatus.PAUSED) {
            throw new JobOperationException("Can only resume paused jobs");
        }

        job.setStatus(JobStatus.SCHEDULED);
        job.setPaused(false);
        schedulerService.resumeJob(jobId);
        Job savedJob = jobRepository.save(job);
        eventPublisher.publishEvent(new JobResumedEvent(this, savedJob));
        return savedJob;
    }
}
