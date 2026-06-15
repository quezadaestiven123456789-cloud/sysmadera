package com.madera.sys_madera.security;

import com.madera.sys_madera.config.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE - 20)
public class BruteForceProtectionFilter extends OncePerRequestFilter {

    private static final String AUTH_PATH = "/api/v1/auth";
    private static final long IDLE_TIMEOUT_MINUTES = 30;

    private final Map<String, BucketEntry> buckets = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final int windowMinutes;

    public BruteForceProtectionFilter(RateLimitProperties properties) {
        this.maxRequests = properties.getMaxRequests();
        this.windowMinutes = properties.getWindowMinutes();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String ip = resolveIp(request);
        BucketEntry entry = buckets.computeIfAbsent(ip, this::createBucket);
        entry.lastAccessMillis = System.currentTimeMillis();

        if (entry.bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        log.warn("Rate limit exceeded for IP: {}", ip);
        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"message\":\"Demasiadas solicitudes. Intente de nuevo en " + windowMinutes + " minuto(s).\"}");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith(AUTH_PATH);
    }

    private String resolveIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private BucketEntry createBucket(String ip) {
        Refill refill = Refill.greedy(maxRequests, Duration.ofMinutes(windowMinutes));
        Bandwidth limit = Bandwidth.classic(maxRequests, refill);
        Bucket bucket = Bucket.builder()
                .addLimit(limit)
                .build();
        return new BucketEntry(bucket);
    }

    @Scheduled(fixedRate = 60_000)
    public void evictStaleEntries() {
        long now = System.currentTimeMillis();
        long timeout = Duration.ofMinutes(IDLE_TIMEOUT_MINUTES).toMillis();
        buckets.entrySet().removeIf(entry ->
                now - entry.getValue().lastAccessMillis > timeout);
    }

    private static class BucketEntry {
        final Bucket bucket;
        volatile long lastAccessMillis;

        BucketEntry(Bucket bucket) {
            this.bucket = bucket;
            this.lastAccessMillis = System.currentTimeMillis();
        }
    }

}
