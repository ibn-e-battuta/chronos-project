package io.shinmen.chronos.job.dto;

import java.time.LocalDateTime;

import io.shinmen.chronos.common.enums.JobStatus;
import io.shinmen.chronos.common.enums.JobType;
import lombok.Data;

@Data
public class JobResponse {
    private Long id;
    private String name;
    private String description;
    private String cronExpression;
    private JobStatus status;
    private JobType jobType;
    private LocalDateTime nextRunTime;
    private Integer retryCount;
    private Integer maxRetries;
    private boolean isPaused;
}
