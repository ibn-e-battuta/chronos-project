package io.shinmen.chronos.job.type.handler;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.shinmen.chronos.common.enums.JobType;
import io.shinmen.chronos.common.exception.JobValidationException;
import io.shinmen.chronos.job.model.Job;

@Component
public class HttpJobHandler extends AbstractJobTypeHandler {
    private final RestTemplate restTemplate;

    public HttpJobHandler(ObjectMapper objectMapper, RestTemplate restTemplate) {
        super(objectMapper);
        this.restTemplate = restTemplate;
    }

    @Override
    public JobType getJobType() {
        return JobType.HTTP_REQUEST;
    }

    @Override
    public void validate(Map<String, Object> config) {
        requireFields(config, "url", "method");
        validateUrl((String) config.get("url"));
        validateHttpMethod((String) config.get("method"));
    }

    @Override
    protected void executeWithConfig(Job job, Map<String, Object> config) {
        String url = (String) config.get("url");
        HttpMethod method = HttpMethod.valueOf((String) config.get("method"));
        String body = (String) config.getOrDefault("body", "");
        Map<String, String> headers = (Map<String, String>) config.getOrDefault("headers", Map.of());

        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach(httpHeaders::add);

        HttpEntity<String> entity = new HttpEntity<>(body, httpHeaders);
        restTemplate.exchange(url, method, entity, String.class);
    }

    private void validateUrl(String url) {
        try {
            new URI(url);
        } catch (Exception e) {
            throw new JobValidationException("Invalid URL format: " + url);
        }
    }

    private void validateHttpMethod(String method) {
        try {
            HttpMethod.valueOf(method.toUpperCase());
        } catch (Exception e) {
            throw new JobValidationException("Invalid HTTP method: " + method);
        }
    }
}
