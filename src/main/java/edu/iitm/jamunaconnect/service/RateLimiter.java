package edu.iitm.jamunaconnect.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Module E: per-IP rate limit on complaint submission, a plain spam deterrent.
 * In-memory is enough for one hostel on one process; swap for a shared store if
 * the app is ever scaled horizontally.
 */
@Service
public class RateLimiter {

    private record Window(Instant start, AtomicInteger count) {
    }

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final int limitPerMinute;

    public RateLimiter(@Value("${app.rate-limit.per-minute:5}") int limitPerMinute) {
        this.limitPerMinute = limitPerMinute;
    }

    /** @return true if the caller is within the limit. */
    public boolean allow(String key) {
        Instant now = Instant.now();
        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || Duration.between(existing.start(), now).toMinutes() >= 1) {
                return new Window(now, new AtomicInteger(1));
            }
            existing.count().incrementAndGet();
            return existing;
        });
        return window.count().get() <= limitPerMinute;
    }
}