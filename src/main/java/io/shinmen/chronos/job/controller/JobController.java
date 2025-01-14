package io.shinmen.chronos.job.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.shinmen.chronos.auth.model.User;
import io.shinmen.chronos.auth.repository.UserRepository;
import io.shinmen.chronos.auth.security.UserDetailsImpl;
import io.shinmen.chronos.common.enums.JobStatus;
import io.shinmen.chronos.common.exception.JobValidationException;
import io.shinmen.chronos.job.dto.JobRequest;
import io.shinmen.chronos.job.dto.JobResponse;
import io.shinmen.chronos.job.model.Job;
import io.shinmen.chronos.job.service.JobService;
import io.shinmen.chronos.job.service.execution.QuartzTriggerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;
    private final QuartzTriggerService quartzTriggerService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @RequestBody JobRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws JsonProcessingException {

        User user = userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Job job = new Job();
        job.setName(request.getName());
        job.setDescription(request.getDescription());
        job.setRunNow(request.isRunNow());
        job.setUser(user);
        
        // Set cron expression only if not running immediately
        if (!request.isRunNow()) {
            if (request.getCronExpression() == null || request.getCronExpression().trim().isEmpty()) {
                throw new JobValidationException("Cron expression is required for scheduled jobs");
            }
            job.setCronExpression(request.getCronExpression());
        }

        job.setMaxRetries(request.getMaxRetries());
        job.setJobType(request.getJobType());
        
        // Convert configuration to JSON string
        String jsonConfig = objectMapper.writeValueAsString(request.getJobConfiguration());
        job.setJobConfiguration(jsonConfig);
        
        job.setStatus(JobStatus.SCHEDULED);

        Job savedJob = jobService.createJob(job);
        return ResponseEntity.ok(convertToResponse(savedJob));
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> getUserJobs(@AuthenticationPrincipal User user) {
        List<Job> jobs = jobService.getUserJobs(user.getId());
        List<JobResponse> responses = jobs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJob(@PathVariable UUID jobId) {
        Job job = jobService.getJob(jobId);
        return ResponseEntity.ok(convertToResponse(job));
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable UUID jobId,
            @RequestBody JobRequest request,
            @AuthenticationPrincipal User user) throws JsonProcessingException {
        Job job = jobService.getJob(jobId);
        if (!job.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        job.setName(request.getName());
        job.setDescription(request.getDescription());
        job.setCronExpression(request.getCronExpression());
        job.setMaxRetries(request.getMaxRetries());
        job.setJobType(request.getJobType());
        job.setJobConfiguration(objectMapper.writeValueAsString(request.getJobConfiguration()));

        Job updatedJob = jobService.updateJob(job);
        return ResponseEntity.ok(convertToResponse(updatedJob));
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> deleteJob(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal User user) {
        Job job = jobService.getJob(jobId);
        if (!job.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        jobService.deleteJob(jobId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{jobId}/cancel")
    public ResponseEntity<JobResponse> cancelJob(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal User user) {
        Job job = jobService.getJob(jobId);
        if (!job.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        jobService.cancelJob(jobId);
        return ResponseEntity.ok(convertToResponse(job));
    }

    private JobResponse convertToResponse(Job job) {
        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setName(job.getName());
        response.setDescription(job.getDescription());
        response.setCronExpression(job.getCronExpression());
        response.setStatus(job.getStatus());
        response.setJobType(job.getJobType());
        response.setPaused(job.isPaused());
        response.setRetryCount(job.getRetryCount());
        response.setMaxRetries(job.getMaxRetries());

        quartzTriggerService.getNextFireTime(job.getId())
                .ifPresent(response::setNextRunTime);

        return response;
    }

    @PostMapping("/{jobId}/pause")
    public ResponseEntity<JobResponse> pauseJob(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal User user) {
        Job job = jobService.getJob(jobId);
        if (!job.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Job pausedJob = jobService.pauseJob(jobId);
        return ResponseEntity.ok(convertToResponse(pausedJob));
    }

    @PostMapping("/{jobId}/resume")
    public ResponseEntity<JobResponse> resumeJob(
            @PathVariable UUID jobId,
            @AuthenticationPrincipal User user) {
        Job job = jobService.getJob(jobId);
        if (!job.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Job resumedJob = jobService.resumeJob(jobId);
        return ResponseEntity.ok(convertToResponse(resumedJob));
    }

}
