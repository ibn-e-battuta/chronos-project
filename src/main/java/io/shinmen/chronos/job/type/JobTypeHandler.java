package io.shinmen.chronos.job.type;

import java.util.Map;

import io.shinmen.chronos.common.enums.JobType;
import io.shinmen.chronos.job.model.Job;

public interface JobTypeHandler {
    void execute(Job job) throws Exception;
    void validate(Map<String, Object> config);
    JobType getJobType();
}
