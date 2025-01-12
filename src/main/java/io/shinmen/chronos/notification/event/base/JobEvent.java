package io.shinmen.chronos.notification.event.base;

import org.springframework.context.ApplicationEvent;

import io.shinmen.chronos.job.model.Job;
import lombok.Getter;

@Getter
public abstract class JobEvent extends ApplicationEvent {
    protected final Job job;

    public JobEvent(Object source, Job job) {
        super(source);
        this.job = job;
    }
}
