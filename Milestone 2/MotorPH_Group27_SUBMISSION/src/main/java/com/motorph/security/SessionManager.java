package com.motorph.security;

import com.motorph.model.User;
import com.motorph.service.AccessDeniedException;
import com.motorph.service.AuthenticationException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Issues random session tokens after login and checks roles on each request. */
public class SessionManager {
    private static final Duration TTL = Duration.ofMinutes(30);
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    private record Session(User user, Instant expiresAt) { }

    public String create(User user) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(token, new Session(user, Instant.now().plus(TTL)));
        return token;
    }

    public User require(String token, Set<String> allowedRoles) {
        if (token == null) throw new AuthenticationException("Please log in first.");
        Session s = sessions.get(token);
        if (s == null || Instant.now().isAfter(s.expiresAt())) {
            sessions.remove(token);
            throw new AuthenticationException("Session expired. Please log in again.");
        }
        if (!allowedRoles.contains(s.user().getRole())) {
            throw new AccessDeniedException("Your role is not allowed to do this.");
        }
        return s.user();
    }

    public void invalidate(String token) {
        if (token != null) sessions.remove(token);
    }
}
