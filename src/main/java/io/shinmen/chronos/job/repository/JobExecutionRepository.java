package io.shinmen.chronos.job.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.shinmen.chronos.job.model.JobExecution;

import java.util.List;
import java.util.UUID;

public interface JobExecutionRepository extends JpaRepository<JobExecution, UUID> {
    List<JobExecution> findByJobId(UUID jobId);
    List<JobExecution> findByJobIdOrderByStartTimeDesc(UUID jobId);
}
