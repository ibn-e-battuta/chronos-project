package io.shinmen.chronos.job.type.handler;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.shinmen.chronos.common.exception.JobValidationException;
import io.shinmen.chronos.job.model.Job;
import io.shinmen.chronos.job.type.JobTypeHandler;

public abstract class AbstractJobTypeHandler implements JobTypeHandler {
    protected final ObjectMapper objectMapper;

    protected AbstractJobTypeHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void execute(Job job) throws Exception {
        Map<String, Object> config = objectMapper.readValue(job.getJobConfiguration(), new TypeReference<Map<String, Object>>() {});
        executeWithConfig(job, config);
    }

    protected abstract void executeWithConfig(Job job, Map<String, Object> config) throws Exception;

    protected void requireFields(Map<String, Object> config, String... fields) {
        for (String field : fields) {
            if (!config.containsKey(field) || config.get(field) == null) {
                throw new JobValidationException("Missing required field: " + field);
            }
        }
    }
}
