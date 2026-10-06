package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * A key used to sign Elements-issued session tokens. The private half is stored encrypted at rest
 * and is never returned in any API response. Tokens carry the {@link #getKid()} in their JOSE header
 * so that verification can select the correct key, allowing keys to be rotated without invalidating
 * outstanding sessions.
 */
@Schema(description = "A key used to sign Elements-issued session tokens.")
public class JwtSigningKey {

    /**
     * The signing algorithm used for Elements-issued session tokens.
     */
    public static final String ALGORITHM = "RS256";

    /**
     * The RSA key length, in bits, used when generating new signing keys.
     */
    public static final int RSA_KEY_LENGTH = 2048;

    @Schema(description = "Uniquely identifies the key. Carried in the token's JOSE header.")
    @NotBlank
    private String kid;

    @Schema(description = "The token signing algorithm used with this key. Always RS256.")
    private String algorithm = ALGORITHM;

    @Schema(description = "The key status.")
    @NotNull
    private Status status;

    @Schema(description = "The Base64 encoded X.509 public key.", readOnly = true)
    private String publicKey;

    @Schema(description = "The private key, encrypted at rest. Never returned in a response.", readOnly = true)
    private String privateKey;

    @Schema(description = "Encryption metadata, present when the private key is encrypted.", readOnly = true)
    private Encryption encryption;

    @Schema(description = "When the key was created, in milliseconds since the epoch.", readOnly = true)
    private long createdAt;

    public enum Status {
        /**
         * The newest active key. Issued tokens are signed with the active key.
         */
        ACTIVE,

        /**
         * A previously active key, retained so that outstanding tokens continue to verify.
         */
        RETIRED
    }

    @Schema(description = "Describes how the private key is encrypted at rest.")
    public static class Encryption {

        @Schema(description = "The encryption family, e.g. AES.")
        private String family;

        @Schema(description = "The cipher algorithm, e.g. AES/CBC/PKCS5Padding.")
        private String algorithm;

        @Schema(description = "The key-derivation algorithm, e.g. PBKDF2WithHmacSHA256.")
        private String secretKeyAlgorithm;

        @Schema(description = "The hex-encoded initialization vector.")
        private String iv;

        @Schema(description = "The hex-encoded key-derivation salt.")
        private String salt;

        @Schema(description = "The key-derivation iteration count.")
        private int iterations;

        @Schema(description = "The derived key length, in bits.")
        private int keyLength;

        public String getFamily() {
            return family;
        }

        public void setFamily(String family) {
            this.family = family;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public String getSecretKeyAlgorithm() {
            return secretKeyAlgorithm;
        }

        public void setSecretKeyAlgorithm(String secretKeyAlgorithm) {
            this.secretKeyAlgorithm = secretKeyAlgorithm;
        }

        public String getIv() {
            return iv;
        }

        public void setIv(String iv) {
            this.iv = iv;
        }

        public String getSalt() {
            return salt;
        }

        public void setSalt(String salt) {
            this.salt = salt;
        }

        public int getIterations() {
            return iterations;
        }

        public void setIterations(int iterations) {
            this.iterations = iterations;
        }

        public int getKeyLength() {
            return keyLength;
        }

        public void setKeyLength(int keyLength) {
            this.keyLength = keyLength;
        }

    }

    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public Encryption getEncryption() {
        return encryption;
    }

    public void setEncryption(Encryption encryption) {
        this.encryption = encryption;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

}
