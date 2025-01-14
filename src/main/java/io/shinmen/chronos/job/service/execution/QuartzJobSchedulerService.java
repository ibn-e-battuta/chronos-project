package io.shinmen.chronos.job.service.execution;

import java.util.UUID;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
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
        try {
            JobDetail jobDetail = buildJobDetail(job);
            Trigger trigger;
            
            if (job.isRunNow()) {
                // For immediate execution, use a simple trigger
                trigger = TriggerBuilder.newTrigger()
                    .withIdentity(job.getId().toString())
                    .forJob(jobDetail)
                    .startNow()
                    .build();
            } else {
                // For scheduled execution, use a cron trigger
                trigger = TriggerBuilder.newTrigger()
                    .withIdentity(job.getId().toString())
                    .forJob(jobDetail)
                    .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()))
                    .build();
            }
            
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("Error scheduling job", e);
        }
    }

    public void updateJob(Job job) {
        deleteJob(job.getId());
        scheduleJob(job);
    }

    private JobDetail buildJobDetail(Job job) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("jobId", job.getId());
        
        return JobBuilder.newJob()
            .ofType(QuartzJobExecutor.class)
            .withIdentity(job.getId().toString())
            .withDescription(job.getDescription())
            .usingJobData(jobDataMap)
            .storeDurably()
            .build();
    }

    private Trigger buildJobTrigger(Job job) {
        TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger()
                .withIdentity(job.getId().toString())
                .withDescription(job.getDescription())
                .forJob(job.getId().toString());

        if (job.isRunNow()) {
            builder.startNow();
            if (job.getCronExpression() != null) {
                builder.withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()));
            }
        } else {
            builder.withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()));
        }

        return builder.build();
    }

    public void deleteJob(UUID jobId) {
        try {
            scheduler.deleteJob(new JobKey(jobId.toString()));
        } catch (SchedulerException e) {
            throw new RuntimeException("Error deleting job", e);
        }
    }

    public void pauseJob(UUID jobId) {
        try {
            scheduler.pauseJob(new JobKey(jobId.toString()));
        } catch (SchedulerException e) {
            throw new RuntimeException("Error pausing job", e);
        }
    }

    public void resumeJob(UUID jobId) {
        try {
            scheduler.resumeJob(new JobKey(jobId.toString()));
        } catch (SchedulerException e) {
            throw new RuntimeException("Error resuming job", e);
        }
    }
}