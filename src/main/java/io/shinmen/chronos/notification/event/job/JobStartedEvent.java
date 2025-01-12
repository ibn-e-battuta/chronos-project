package io.shinmen.chronos.notification.event.job;

import io.shinmen.chronos.job.model.Job;
import io.shinmen.chronos.notification.event.base.JobEvent;
import lombok.Getter;

@Getter
public class JobStartedEvent extends JobEvent {
    private final String executionId;

    public JobStartedEvent(Object source, Job job, String executionId) {
        super(source, job);
        this.executionId = executionId;
    }
}
