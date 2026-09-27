package edu.iitm.jamunaconnect.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterTest {

    @Test
    void blocksBeyondTheConfiguredLimit() {
        RateLimiter limiter = new RateLimiter(3);
        assertThat(limiter.allow("1.2.3.4")).isTrue();
        assertThat(limiter.allow("1.2.3.4")).isTrue();
        assertThat(limiter.allow("1.2.3.4")).isTrue();
        assertThat(limiter.allow("1.2.3.4")).isFalse();
    }

    @Test
    void countsArePerKey() {
        RateLimiter limiter = new RateLimiter(1);
        assertThat(limiter.allow("1.1.1.1")).isTrue();
        assertThat(limiter.allow("1.1.1.1")).isFalse();
        assertThat(limiter.allow("2.2.2.2")).isTrue();
    }
}