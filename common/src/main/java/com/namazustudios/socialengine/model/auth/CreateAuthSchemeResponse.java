package com.namazustudios.socialengine.model.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@ApiModel(description = "Represents a response from creating an Auth Scheme for an Application.")
public class CreateAuthSchemeResponse {

    @NotNull
    @ApiModelProperty("The full JSON response as described in AuthScheme")
    private AuthScheme scheme;

    @NotNull
    @ApiModelProperty(
        "The Base64 public key that was either given or generated during creation. " +
        "See https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/spec/X509EncodedKeySpec.html " +
        "for details on the specifics of the format.")
    private String publicKey;

    @ApiModelProperty(
        "The Base64 public key that was either given or generated during creation. " +
        "See https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/spec/PKCS8EncodedKeySpec.html " +
        "for details on the specifics of the format.")
    private String privateKey;

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }

    public AuthScheme getScheme() {
        return scheme;
    }

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
