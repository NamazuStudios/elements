package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;

/** Represents a response from updating an Auth Scheme for an Application. */
@Schema(description = "Represents a response from updating an Auth Scheme for an Application.")
public class UpdateAuthSchemeResponse {

    /** Creates a new instance. */
    public UpdateAuthSchemeResponse() {}

    /** The full auth scheme response. */
    @Schema(description = "The full JSON response as described in AuthScheme")
    public AuthScheme scheme;

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
     * Returns the full auth scheme.
     *
     * @return the scheme
     */
    public AuthScheme getScheme() {
        return scheme;
    }

    /**
     * Sets the full auth scheme.
     *
     * @param scheme the scheme
     */
    public void setScheme(AuthScheme scheme) {
        this.scheme = scheme;
    }

    /**
     * Returns the Base64-encoded public key.
     *
     * @return the public key
     */
    public String getPublicKey() { return publicKey; }

    /**
     * Sets the Base64-encoded public key.
     *
     * @param publicKey the public key
     */
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    /**
     * Returns the Base64-encoded private key.
     *
     * @return the private key
     */
    public String getPrivateKey() { return privateKey; }

    /**
     * Sets the Base64-encoded private key.
     *
     * @param privateKey the private key
     */
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }

}
