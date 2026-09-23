package dev.getelements.elements.service.auth.totp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Hashes recovery codes for storage, so the raw codes are never retrievable once issued -- mirrors the
 * write-once-readable-only-at-generation-time rule already used for the OIDC client secret and the CAPTCHA
 * secret key. Recovery codes are already high-entropy, randomly generated values (see
 * {@code dev.samstevens.totp.recovery.RecoveryCodeGenerator}), so a plain digest is sufficient -- no salt or
 * per-code work factor is needed the way it is for user-chosen passwords.
 */
public final class TotpRecoveryCodeHasher {

    private TotpRecoveryCodeHasher() {}

    /**
     * Normalizes (trims, lowercases) and hashes a recovery code for storage or lookup.
     *
     * @param code the raw recovery code as entered by the user
     * @return the hashed code
     */
    public static String hash(final String code) {
        try {
            final var normalized = code.trim().toLowerCase();
            final var digest = MessageDigest.getInstance("SHA-256");
            final var hash = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }

}
