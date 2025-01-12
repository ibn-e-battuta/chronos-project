package io.shinmen.chronos.notification.event.job;

import io.shinmen.chronos.job.model.Job;
import io.shinmen.chronos.notification.event.base.JobEvent;
import lombok.Getter;

@Getter
public class JobResumedEvent extends JobEvent {
    public JobResumedEvent(Object source, Job job) {
        super(source, job);
    }
}
