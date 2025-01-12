package io.shinmen.chronos.notification.event.job;

import io.shinmen.chronos.job.model.Job;
import io.shinmen.chronos.notification.event.base.JobEvent;
import lombok.Getter;

@Getter
public class JobFailureEvent extends JobEvent {
    private final String error;

    public JobFailureEvent(Object source, Job job, String error) {
        super(source, job);
        this.error = error;
    }
}
