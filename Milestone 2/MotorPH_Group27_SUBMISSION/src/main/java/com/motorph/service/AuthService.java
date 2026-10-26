package com.motorph.service;

import com.motorph.dao.UserDAO;
import com.motorph.model.User;
import com.motorph.security.PasswordUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

/**
 * CONTROL #2 (part 1): Authentication + Legacy Migration Handler.
 * Hashing itself is delegated to PasswordUtil (Argon2id).
 *
 * Note: this class never sees database credentials. It only talks to UserDAO.
 */
public class AuthService {

    private final UserDAO userDAO;

    // Used when the username does not exist, so the response takes the same time
    // as a real check. This stops attackers from guessing which usernames exist.
    private final String dummyHash;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.dummyHash = hash("timing-equalizer-dummy".toCharArray());
    }

    public String hash(char[] password) {
        return PasswordUtil.hash(password);
    }

    /**
     * @return the authenticated user
     * @throws AuthenticationException on any failure (same message every time)
     */
    public User login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new ValidationException("Username and password are required.");
        }

        Optional<User> found = userDAO.findByUsername(username.trim());
        if (found.isEmpty()) {
            PasswordUtil.verify(dummyHash, password.toCharArray()); // equalize timing
            throw new AuthenticationException("Invalid username or password.");
        }

        User user = found.get();
        String stored = user.getPasswordHash();

        if (PasswordUtil.isArgon2idHash(stored)) {
            // Normal path: Argon2id verify (constant-time comparison inside the library).
            if (!PasswordUtil.verify(stored, password.toCharArray())) {
                throw new AuthenticationException("Invalid username or password.");
            }
            return user;
        }

        // Legacy path: record is still plaintext from the old system.
        // Compare in constant time, then immediately upgrade it to Argon2id.
        boolean match = MessageDigest.isEqual(
                stored.getBytes(StandardCharsets.UTF_8),
                password.getBytes(StandardCharsets.UTF_8));
        if (!match) {
            PasswordUtil.verify(dummyHash, password.toCharArray()); // equalize timing
            throw new AuthenticationException("Invalid username or password.");
        }
        String newHash = hash(password.toCharArray());
        userDAO.updatePassword(user.getUserId(), newHash);
        System.out.println("[AuthService] Legacy plaintext password migrated to Argon2id for user_id="
                + user.getUserId());
        return new User(user.getUserId(), user.getUsername(), newHash, user.getRole(), user.getEmployeeId());
    }
}
