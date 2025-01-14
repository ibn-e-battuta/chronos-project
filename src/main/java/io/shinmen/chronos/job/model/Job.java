package io.shinmen.chronos.job.model;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.quartz.CronExpression;

import io.shinmen.chronos.auth.model.User;
import io.shinmen.chronos.common.enums.JobStatus;
import io.shinmen.chronos.common.enums.JobType;
import io.shinmen.chronos.common.exception.JobValidationException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "jobs")
@Data
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "cron_expression", nullable = true)
    private String cronExpression;

    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.SCHEDULED;

    private boolean isPaused = false;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String jobConfiguration;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Integer retryCount = 0;
    private Integer maxRetries = 3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private Set<JobExecution> executions = new HashSet<>();

    private boolean runNow = false;

    private LocalDateTime nextRunTime;  // Add this field

    @PrePersist
    @PreUpdate
    public void updateNextRunTime() {
        if (this.cronExpression != null && !this.cronExpression.isEmpty()) {
            try {
                CronExpression cron = new CronExpression(this.cronExpression);
                this.nextRunTime = LocalDateTime.ofInstant(cron.getNextValidTimeAfter(new Date()).toInstant(), 
                    ZoneId.systemDefault());
            } catch (ParseException e) {
                throw new JobValidationException("Invalid cron expression: " + this.cronExpression);
            }
        } else if (this.isRunNow()) {
            this.nextRunTime = LocalDateTime.now();
        }
    }
}