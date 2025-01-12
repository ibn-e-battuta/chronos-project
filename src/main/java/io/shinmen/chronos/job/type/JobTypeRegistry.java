package io.shinmen.chronos.job.type;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import io.shinmen.chronos.common.enums.JobType;
import jakarta.annotation.PostConstruct;

@Component
public class JobTypeRegistry {
    private final Map<JobType, JobTypeHandler> handlers = new HashMap<>();
    private final ApplicationContext applicationContext;

    public JobTypeRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void registerHandlers() {
        applicationContext.getBeansOfType(JobTypeHandler.class)
                .values()
                .forEach(handler -> handlers.put(handler.getJobType(), handler));
    }

    public JobTypeHandler getHandler(JobType jobType) {
        JobTypeHandler handler = handlers.get(jobType);
        if (handler == null) {
            throw new IllegalArgumentException("No handler registered for job type: " + jobType);
        }
        return handler;
    }
}
