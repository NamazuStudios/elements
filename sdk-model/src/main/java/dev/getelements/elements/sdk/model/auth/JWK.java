package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;

public class JWK implements Serializable {

    @Schema(description = "Algorithm (e.g. RS256)")
    private String alg;

    @Schema(description = "Key id (unique to issuer)")
    private String kid;

    @Schema(description = "Key type (e.g. RSA)")
    private String kty;

    @Schema(description = "The intended use (e.g. sig)")
    private String use;

    @Schema(description = "Base64url encoded exponent")
    private String e;

    @Schema(description = "Pub key modulus")
    private String n;

    public JWK() {}

    public JWK(String alg, String kid, String kty, String use, String e, String n) {
        this.alg = alg;
        this.kid = kid;
        this.kty = kty;
        this.use = use;
        this.e = e;
        this.n = n;
    }

    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    public String getKty() {
        return kty;
    }

    public void setKty(String kty) {
        this.kty = kty;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public String getE() {
        return e;
    }

    public void setE(String e) {
        this.e = e;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JWK key = (JWK) o;
        return Objects.equals(getAlg(), key.getAlg()) &&
                Objects.equals(getKid(), key.getKid()) &&
                Objects.equals(getKty(), key.getKty()) &&
                Objects.equals(getUse(), key.getUse()) &&
                Objects.equals(getE(), key.getE()) &&
                Objects.equals(getN(), key.getN());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAlg(), getKid(), getKty(), getUse(), getE(), getN());
    }

    @Override
    public String toString() {
        return "JWK{" +
                "alg='" + alg + '\'' +
                ", kid='" + kid + '\'' +
                ", kty='" + kty + '\'' +
                ", use='" + use + '\'' +
                ", e='" + e + '\'' +
                ", n='" + n + '\'' +
                '}';
    }
}