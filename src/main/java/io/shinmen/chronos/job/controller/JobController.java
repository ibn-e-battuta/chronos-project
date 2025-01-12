package io.shinmen.chronos.job.controller;

import java.util.List;
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

import io.shinmen.chronos.auth.model.User;
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

    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @RequestBody JobRequest request,
            @AuthenticationPrincipal User user) throws JsonProcessingException {
        Job job = new Job();
        job.setName(request.getName());
        job.setDescription(request.getDescription());
        job.setCronExpression(request.getCronExpression());
        job.setMaxRetries(request.getMaxRetries());
        job.setJobType(request.getJobType());
        job.setJobConfiguration(request.getJobConfiguration());
        job.setRunNow(request.isRunNow());
        job.setUser(user);

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
    public ResponseEntity<JobResponse> getJob(@PathVariable Long jobId) {
        Job job = jobService.getJob(jobId);
        return ResponseEntity.ok(convertToResponse(job));
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long jobId,
            @RequestBody JobRequest request,
            @AuthenticationPrincipal User user) {
        Job job = jobService.getJob(jobId);
        if (!job.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        job.setName(request.getName());
        job.setDescription(request.getDescription());
        job.setCronExpression(request.getCronExpression());
        job.setMaxRetries(request.getMaxRetries());
        job.setJobType(request.getJobType());
        job.setJobConfiguration(request.getJobConfiguration());

        Job updatedJob = jobService.updateJob(job);
        return ResponseEntity.ok(convertToResponse(updatedJob));
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> deleteJob(
            @PathVariable Long jobId,
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
            @PathVariable Long jobId,
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
            @PathVariable Long jobId,
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
            @PathVariable Long jobId,
            @AuthenticationPrincipal User user) {
        Job job = jobService.getJob(jobId);
        if (!job.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Job resumedJob = jobService.resumeJob(jobId);
        return ResponseEntity.ok(convertToResponse(resumedJob));
    }

    
}
