package io.shinmen.chronos.job.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import io.shinmen.chronos.auth.model.User;
import io.shinmen.chronos.common.enums.JobStatus;
import io.shinmen.chronos.common.enums.JobType;
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
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Table(name = "jobs")
@Data
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String cronExpression;

    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.SCHEDULED;

    private boolean isPaused = false;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @Column(columnDefinition = "jsonb")
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
}