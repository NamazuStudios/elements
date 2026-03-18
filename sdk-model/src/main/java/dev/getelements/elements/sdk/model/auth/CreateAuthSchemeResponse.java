package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/** Represents a response from creating an Auth Scheme for an Application. */
@Schema(description = "Represents a response from creating an Auth Scheme for an Application.")
public class CreateAuthSchemeResponse {

    /** Creates a new instance. */
    public CreateAuthSchemeResponse() {}

    @NotNull
    @Schema(description = "The full JSON response as described in AuthScheme")
    private AuthScheme scheme;

    @NotNull
    @Schema(description =
        "The Base64 public key that was either given or generated during creation. " +
        "See https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/spec/X509EncodedKeySpec.html " +
        "for details on the specifics of the format.")
    private String publicKey;

    @Schema(description =
        "The Base64 public key that was either given or generated during creation. " +
        "See https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/spec/PKCS8EncodedKeySpec.html " +
        "for details on the specifics of the format.")
    private String privateKey;

    /**
     * Returns the base-64 encoded public key.
     *
     * @return the public key
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * Sets the base-64 encoded public key.
     *
     * @param publicKey the public key
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Returns the base-64 encoded private key, if generated.
     *
     * @return the private key
     */
    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * Sets the base-64 encoded private key.
     *
     * @param privateKey the private key
     */
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }

    /**
     * Returns the created auth scheme.
     *
     * @return the scheme
     */
    public AuthScheme getScheme() {
        return scheme;
    }

    /**
     * Sets the created auth scheme.
     *
     * @param scheme the scheme
     */
    public void setScheme(AuthScheme scheme) {
        this.scheme = scheme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateAuthSchemeResponse that = (CreateAuthSchemeResponse) o;
        return Objects.equals(getScheme(), that.getScheme()) && Objects.equals(getPublicKey(), that.getPublicKey()) && Objects.equals(getPrivateKey(), that.getPrivateKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getScheme(), getPublicKey(), getPrivateKey());
    }
}
