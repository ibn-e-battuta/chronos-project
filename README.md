# Chronos - Job Scheduler System

## Overview
Chronos is a robust and scalable job scheduling system built with Spring Boot. It supports various job types, recurring schedules, comprehensive job management, and monitoring capabilities.

## Features
- Job submission with immediate or scheduled execution
- Recurring jobs using cron expressions
- Complete job lifecycle management (create, pause, resume, cancel)
- Automatic retry mechanism for failed jobs
- Comprehensive logging and monitoring
- JWT-based authentication
- Prometheus metrics integration

## Prerequisites
- Docker and Docker Compose
- Java 21 (for local development)
- Maven (for local development)

## Quick Start

1. Clone the repository:
```bash
git clone <repository-url>
cd chronos
```

2. Start the application using Docker Compose:
```bash
docker-compose up -d
```

The application will be available at http://localhost:8080

## Available Services
- Application: http://localhost:8080
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000
- PostgreSQL: localhost:5432

## API Documentation

### Authentication
```bash
# Register a new user
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password","email":"test@example.com"}'

# Login
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password"}'
```

### Job Management

#### Create a Job
```bash
# Create an HTTP job
curl -X POST http://localhost:8080/api/jobs \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test HTTP Job",
    "description": "Periodic HTTP GET request",
    "cronExpression": "0 */5 * * * ?",
    "jobType": "HTTP_REQUEST",
    "jobConfiguration": "{\"url\":\"https://api.example.com\",\"method\":\"GET\"}",
    "maxRetries": 3
  }'

# Create an Email job
curl -X POST http://localhost:8080/api/jobs \
  -H "Authorization: Bearer <your-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Daily Report",
    "description": "Send daily report email",
    "cronExpression": "0 0 12 * * ?",
    "jobType": "EMAIL",
    "jobConfiguration": "{\"to\":\"recipient@example.com\",\"subject\":\"Daily Report\",\"body\":\"Here is your daily report.\"}",
    "maxRetries": 3
  }'
```

#### Manage Jobs
```bash
# List all jobs
curl -X GET http://localhost:8080/api/jobs \
  -H "Authorization: Bearer <your-token>"

# Get specific job
curl -X GET http://localhost:8080/api/jobs/{jobId} \
  -H "Authorization: Bearer <your-token>"

# Pause job
curl -X POST http://localhost:8080/api/jobs/{jobId}/pause \
  -H "Authorization: Bearer <your-token>"

# Resume job
curl -X POST http://localhost:8080/api/jobs/{jobId}/resume \
  -H "Authorization: Bearer <your-token>"

# Cancel job
curl -X POST http://localhost:8080/api/jobs/{jobId}/cancel \
  -H "Authorization: Bearer <your-token>"
```

## Adding New Job Types

To add a new job type:

1. Add the new job type to `JobType.java` enum
2. Create a new handler class implementing `JobTypeHandler` interface
3. Register the handler in `JobTypeRegistry`

Example of a new job type handler:

```java
@Component
public class CustomJobHandler extends AbstractJobTypeHandler {
    public CustomJobHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public JobType getJobType() {
        return JobType.CUSTOM;
    }

    @Override
    public void validate(Map<String, Object> config) {
        requireFields(config, "required_field1", "required_field2");
    }

    @Override
    protected void executeWithConfig(Job job, Map<String, Object> config) {
        // Implementation
    }
}
```

## Monitoring

The application exposes various metrics through Prometheus at `/actuator/prometheus`. Key metrics include:
- Job execution success/failure rates
- Job duration
- System health indicators

Access Grafana at http://localhost:3000 (default credentials: admin/admin) to visualize these metrics.

## Logging

Logs are stored in the `logs` directory with the following structure:
- `application.log`: General application logs
- `job-execution.log`: Detailed job execution logs
- `security.log`: Authentication and authorization logs

## Development

For local development:

```bash
./mvnw spring-boot:run
```

Run tests:

```bash
./mvnw test
```

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request