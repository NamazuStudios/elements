package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;

@Schema(description = "Represents a response from updating an Auth Scheme for an Application.")
public class UpdateAuthSchemeResponse {

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

    public AuthScheme getScheme() {
        return scheme;
    }

    public void setScheme(AuthScheme scheme) {
        this.scheme = scheme;
    }

    public String getPublicKey() { return publicKey; }

    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public String getPrivateKey() { return privateKey; }

    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }

}
