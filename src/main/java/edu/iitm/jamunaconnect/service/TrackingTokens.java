package edu.iitm.jamunaconnect.service;

import java.security.SecureRandom;
import java.util.Base64;

/** Random, URL-safe complaint secrets; never derived from the sequential id. */
public final class TrackingTokens {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int BYTES = 12;

    private TrackingTokens() {
    }

    public static String newToken() {
        byte[] bytes = new byte[BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
