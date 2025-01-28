package io.shinmen.chronos.common.config;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RateLimitingConfig implements WebMvcConfigurer {

    private final ConcurrentHashMap<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    @Bean
    RateLimiterRegistry rateLimiterRegistry() {
        return RateLimiterRegistry.of(RateLimiterConfig.ofDefaults());
    }

    private RateLimiterConfig getRateLimitConfig(String endpoint) {
        // Auth endpoints - Strict limiting to prevent brute force
        if (endpoint.startsWith("/api/auth/")) {
            return RateLimiterConfig.custom()
                    .limitForPeriod(5)
                    .limitRefreshPeriod(Duration.ofMinutes(1))
                    .timeoutDuration(Duration.ofSeconds(1))
                    .build();
        }

        // Job creation/update - Moderate limiting
        if (endpoint.startsWith("/api/jobs") && (endpoint.equals("/api/jobs") || endpoint.contains("/update"))) {
            return RateLimiterConfig.custom()
                    .limitForPeriod(30)
                    .limitRefreshPeriod(Duration.ofMinutes(1))
                    .timeoutDuration(Duration.ofSeconds(1))
                    .build();
        }

        // Job status checks - More lenient
        if (endpoint.startsWith("/api/jobs") && endpoint.contains("/status")) {
            return RateLimiterConfig.custom()
                    .limitForPeriod(60)
                    .limitRefreshPeriod(Duration.ofMinutes(1))
                    .timeoutDuration(Duration.ofSeconds(1))
                    .build();
        }

        // Job cancellation - Moderate limiting
        if (endpoint.contains("/cancel")) {
            return RateLimiterConfig.custom()
                    .limitForPeriod(20)
                    .limitRefreshPeriod(Duration.ofMinutes(1))
                    .timeoutDuration(Duration.ofSeconds(1))
                    .build();
        }

        // List jobs - Moderate limiting
        if (endpoint.equals("/api/jobs")) {
            return RateLimiterConfig.custom()
                    .limitForPeriod(30)
                    .limitRefreshPeriod(Duration.ofMinutes(1))
                    .timeoutDuration(Duration.ofSeconds(1))
                    .build();
        }

        // Default rate limit for other endpoints
        return RateLimiterConfig.custom()
                .limitForPeriod(50)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ofSeconds(1))
                .build();
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(@NonNull HttpServletRequest request,
                    @NonNull HttpServletResponse response,
                    @NonNull Object handler) {
                String endpoint = request.getRequestURI();
                String clientId = getClientIdentifier(request);
                String rateLimitKey = clientId + "-" + endpoint;

                RateLimiter rateLimiter = rateLimiterMap.computeIfAbsent(
                        rateLimitKey,
                        k -> RateLimiter.of(endpoint, getRateLimitConfig(endpoint)));

                if (!rateLimiter.acquirePermission()) {
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    return false;
                }

                return true;
            }
        });
    }

    private String getClientIdentifier(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-FORWARDED-FOR");
        if (xForwardedFor != null) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
