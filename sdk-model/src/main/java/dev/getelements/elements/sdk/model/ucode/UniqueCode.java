package dev.getelements.elements.sdk.model.ucode;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

/**
 * Represents a unique code with an identifier, the code itself, and an expiration timestamp.
 */
@Schema(description = "Represents a unique code with an identifier, the code itself, and an expiration timestamp.")
public class UniqueCode {

    private String id;

    private String code;

    private long linger;

    private long timeout;

    private long expiresAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        UniqueCode that = (UniqueCode) object;
        return expiresAt == that.expiresAt && Objects.equals(id, that.id) && Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, expiresAt);
    }

    @Override
    public String toString() {
        return "UniqueCode{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", expiresAt=" + expiresAt +
                '}';
    }

}
