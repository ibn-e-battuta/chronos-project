package io.shinmen.chronos.job.dto;

import java.util.Map;

import io.shinmen.chronos.common.enums.JobType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobRequest {
    @NotBlank(message = "Job name is required")
    private String name;

    @NotBlank(message = "Job description is required")
    private String description;

    private String cronExpression;

    private Integer maxRetries;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    @NotNull(message = "Job configuration is required")
    private Map<String, Object> jobConfiguration; 
    
    private boolean runNow = false;

    // Custom validation method for scheduling logic
    @AssertTrue(message = "Either cronExpression must be provided or runNow must be true")
    private boolean isValidScheduling() {
        return runNow || (cronExpression != null && !cronExpression.trim().isEmpty());
    }
}
