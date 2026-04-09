package dev.getelements.elements.sdk.model.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.sql.Timestamp;

/**
 * Represents a single-use, time-limited token used to verify ownership of an email {@link UserUid}.
 *
 * <p>The token value is a randomly-generated UUID v4 string used as the database primary key.
 * UUID v4 is chosen over a database-native identifier (e.g. MongoDB {@code ObjectId}) because
 * {@code ObjectId} encodes structural metadata — creation timestamp, machine identifier, and an
 * incrementing counter — making it partially predictable. UUID v4 is produced by
 * {@code SecureRandom} and provides 122 bits of cryptographic randomness with no structural
 * leakage, which is required for a credential embedded in an email URL.
 */
@Schema(description = "A single-use, time-limited token used to verify ownership of an email UID.")
public class UidVerificationToken {

    @Schema(description = "The opaque verification token. A UUID v4 string generated via SecureRandom, "
            + "used as both the database primary key and the value embedded in the verification link. "
            + "UUID v4 is used in preference to a database-native identifier (e.g. MongoDB ObjectId) "
            + "because ObjectId encodes a timestamp, machine ID, and counter, making it partially "
            + "predictable. UUID v4 provides 122 bits of cryptographic randomness with no structural leakage.")
    private String token;

    @Schema(description = "The user that owns the UID being verified.")
    private User user;

    @Schema(description = "The authentication scheme of the UID being verified.")
    private String scheme;

    @Schema(description = "The id value of the UID being verified (the email address for email-scheme UIDs).")
    private String uidId;

    @Schema(description = "The UTC timestamp after which this token is no longer valid. "
            + "Tokens are also automatically removed by a database TTL index on this field.")
    private Timestamp expiry;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getUidId() {
        return uidId;
    }

    public void setUidId(String uidId) {
        this.uidId = uidId;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

}
