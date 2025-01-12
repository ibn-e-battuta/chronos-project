package io.shinmen.chronos.job.service.execution;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

import io.shinmen.chronos.job.model.Job;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuartzJobSchedulerService {
    private final Scheduler scheduler;

    public void scheduleJob(Job job) {
        JobDetail jobDetail = buildJobDetail(job);
        Trigger trigger = buildJobTrigger(job);

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("Error scheduling job", e);
        }
    }

    public void updateJob(Job job) {
        deleteJob(job.getId());
        scheduleJob(job);
    }

    public void deleteJob(Long jobId) {
        try {
            scheduler.deleteJob(new JobKey(jobId.toString()));
        } catch (SchedulerException e) {
            throw new RuntimeException("Error deleting job", e);
        }
    }

    private JobDetail buildJobDetail(Job job) {
        return JobBuilder.newJob(QuartzJobExecutor.class)
                .withIdentity(job.getId().toString())
                .withDescription(job.getDescription())
                .usingJobData("jobId", job.getId())
                .storeDurably()
                .build();
    }

    private Trigger buildJobTrigger(Job job) {
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(job.getId().toString());
                
        if (job.isRunNow()) {
            triggerBuilder.startNow();
            if (job.getCronExpression() != null && !job.getCronExpression().isEmpty()) {
                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()));
            }
        } else {
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()));
        }
        
        return triggerBuilder.build();
    }

    public void pauseJob(Long jobId) {
        try {
            scheduler.pauseJob(new JobKey(jobId.toString()));
        } catch (SchedulerException e) {
            throw new RuntimeException("Error pausing job", e);
        }
    }

    public void resumeJob(Long jobId) {
        try {
            scheduler.resumeJob(new JobKey(jobId.toString()));
        } catch (SchedulerException e) {
            throw new RuntimeException("Error resuming job", e);
        }
    }
}

