package io.shinmen.chronos.job.repository;

import io.shinmen.chronos.common.enums.JobStatus;
import io.shinmen.chronos.job.model.Job;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByUserId(Long userId);
    List<Job> findByStatus(JobStatus status);

    @Query("SELECT j FROM Job j WHERE j.nextRunTime <= ?1 AND j.status = ?2")
    List<Job> findJobsDueForExecution(LocalDateTime now, JobStatus status);
}
