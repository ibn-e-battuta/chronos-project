package io.shinmen.chronos.job.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.shinmen.chronos.job.model.JobExecution;

import java.util.List;

public interface JobExecutionRepository extends JpaRepository<JobExecution, Long> {
    List<JobExecution> findByJobId(Long jobId);
    List<JobExecution> findByJobIdOrderByStartTimeDesc(Long jobId);
}
