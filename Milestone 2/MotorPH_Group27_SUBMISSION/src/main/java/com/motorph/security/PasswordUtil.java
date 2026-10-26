package com.motorph.security;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

/**
 * CONTROL #2: Argon2id hashing utility (argon2-jvm 2.11).
 *
 * hash():   argon2.hash(3, 65536, 1, password)  -> "$argon2id$v=19$m=65536,t=3,p=1$salt$hash"
 * verify(): argon2.verify(hash, candidate)      -> constant-time comparison inside the library
 */
public final class PasswordUtil {

    // OWASP-aligned parameters: 3 iterations, 64 MB memory, 1 thread.
    public static final int ITERATIONS = 3;
    public static final int MEMORY_KB = 65536;
    public static final int PARALLELISM = 1;
    public static final String ARGON2ID_PREFIX = "$argon2id$";

    private static final Argon2 ARGON2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    private PasswordUtil() { }

    public static String hash(char[] password) {
        try {
            return ARGON2.hash(ITERATIONS, MEMORY_KB, PARALLELISM, password);
        } finally {
            ARGON2.wipeArray(password); // erase the plaintext from memory
        }
    }

    public static boolean verify(String hash, char[] candidate) {
        try {
            return ARGON2.verify(hash, candidate);
        } finally {
            ARGON2.wipeArray(candidate);
        }
    }

    public static boolean isArgon2idHash(String stored) {
        return stored != null && stored.startsWith(ARGON2ID_PREFIX);
    }
}
