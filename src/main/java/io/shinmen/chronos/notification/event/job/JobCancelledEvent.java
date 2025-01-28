package io.shinmen.chronos.notification.event.job;

import io.shinmen.chronos.job.model.Job;
import io.shinmen.chronos.notification.event.base.JobEvent;
import lombok.Getter;

@Getter
public class JobCancelledEvent extends JobEvent {
    private final String reason;

    public JobCancelledEvent(Object source, Job job, String reason) {
        super(source, job);
        this.reason = reason;
    }
}
