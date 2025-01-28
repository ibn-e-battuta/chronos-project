package io.shinmen.chronos.notification.event.job;

import io.shinmen.chronos.job.model.Job;
import io.shinmen.chronos.notification.event.base.JobEvent;
import lombok.Getter;

@Getter
public class JobSuccessEvent extends JobEvent {
    private final String result;

    public JobSuccessEvent(Object source, Job job, String result) {
        super(source, job);
        this.result = result;
    }
}
