package edu.iitm.jamunaconnect.service;

import org.springframework.stereotype.Service;

/**
 * Login throttling: a per-IP and per-username minute window on top of the
 * filing limiter, so a guessable staff password cannot be brute-forced.
 * In-memory is appropriate for the single-instance deployment.
 */
@Service
public class LoginAttemptService {

    private final RateLimiter byIp = new RateLimiter(10);
    private final RateLimiter byUsername = new RateLimiter(5);

    public boolean allow(String ip, String username) {
        boolean ipAllowed = byIp.allow("login-ip:" + (ip == null ? "unknown" : ip));
        String userKey = username == null ? "" : username.trim().toLowerCase();
        boolean userAllowed = byUsername.allow("login-user:" + userKey);
        return ipAllowed && userAllowed;
    }
}
