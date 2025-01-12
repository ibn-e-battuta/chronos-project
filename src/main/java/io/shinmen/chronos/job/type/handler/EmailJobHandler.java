package io.shinmen.chronos.job.type.handler;

import java.util.Map;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.shinmen.chronos.common.enums.JobType;
import io.shinmen.chronos.common.exception.JobValidationException;
import io.shinmen.chronos.job.model.Job;

@Component
public class EmailJobHandler extends AbstractJobTypeHandler {
    private final JavaMailSender mailSender;

    public EmailJobHandler(ObjectMapper objectMapper, JavaMailSender mailSender) {
        super(objectMapper);
        this.mailSender = mailSender;
    }

    @Override
    public JobType getJobType() {
        return JobType.EMAIL;
    }

    @Override
    public void validate(Map<String, Object> config) {
        requireFields(config, "to", "subject", "body");
        validateEmail((String) config.get("to"));
    }

    @Override
    protected void executeWithConfig(Job job, Map<String, Object> config) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo((String) config.get("to"));
        message.setSubject((String) config.get("subject"));
        message.setText((String) config.get("body"));
        mailSender.send(message);
    }

    private void validateEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!email.matches(emailRegex)) {
            throw new JobValidationException("Invalid email format: " + email);
        }
    }
}
