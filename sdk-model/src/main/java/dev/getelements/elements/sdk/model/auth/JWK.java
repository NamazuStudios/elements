package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;

/** Represents a JSON Web Key (JWK) as defined by RFC 7517. */
public class JWK implements Serializable {

    @Schema(description = "Algorithm (e.g. RS256)")
    private String alg;

    @Schema(description = "Key id (unique to issuer)")
    private String kid;

    @Schema(description = "Key type (e.g. RSA)")
    private String kty;

    @Schema(description = "The intended use (e.g. sig)")
    private String use;

    @Schema(description = "Base64url encoded exponent (RSA keys only)")
    private String e;

    @Schema(description = "Pub key modulus (RSA keys only)")
    private String n;

    @Schema(description = "Curve name, e.g. P-256, P-384, P-521 (EC keys only)")
    private String crv;

    @Schema(description = "Base64url encoded x coordinate (EC keys only)")
    private String x;

    @Schema(description = "Base64url encoded y coordinate (EC keys only)")
    private String y;

    /** Creates a new empty instance. */
    public JWK() {}

    /**
     * Creates a new instance with the RSA public key fields.
     *
     * @param alg the algorithm
     * @param kid the key ID
     * @param kty the key type
     * @param use the intended use
     * @param e the base64url encoded exponent
     * @param n the public key modulus
     */
    public JWK(String alg, String kid, String kty, String use, String e, String n) {
        this.alg = alg;
        this.kid = kid;
        this.kty = kty;
        this.use = use;
        this.e = e;
        this.n = n;
    }

    /**
     * Returns the algorithm.
     *
     * @return the algorithm
     */
    public String getAlg() {
        return alg;
    }

    /**
     * Sets the algorithm.
     *
     * @param alg the algorithm
     */
    public void setAlg(String alg) {
        this.alg = alg;
    }

    /**
     * Returns the key ID.
     *
     * @return the key ID
     */
    public String getKid() {
        return kid;
    }

    /**
     * Sets the key ID.
     *
     * @param kid the key ID
     */
    public void setKid(String kid) {
        this.kid = kid;
    }

    /**
     * Returns the key type.
     *
     * @return the key type
     */
    public String getKty() {
        return kty;
    }

    /**
     * Sets the key type.
     *
     * @param kty the key type
     */
    public void setKty(String kty) {
        this.kty = kty;
    }

    /**
     * Returns the intended use.
     *
     * @return the intended use
     */
    public String getUse() {
        return use;
    }

    /**
     * Sets the intended use.
     *
     * @param use the intended use
     */
    public void setUse(String use) {
        this.use = use;
    }

    /**
     * Returns the base64url encoded exponent.
     *
     * @return the exponent
     */
    public String getE() {
        return e;
    }

    /**
     * Sets the base64url encoded exponent.
     *
     * @param e the exponent
     */
    public void setE(String e) {
        this.e = e;
    }

    /**
     * Returns the public key modulus.
     *
     * @return the modulus
     */
    public String getN() {
        return n;
    }

    /**
     * Sets the public key modulus.
     *
     * @param n the modulus
     */
    public void setN(String n) {
        this.n = n;
    }

    /**
     * Returns the curve name (e.g. P-256).
     *
     * @return the curve name
     */
    public String getCrv() {
        return crv;
    }

    /**
     * Sets the curve name (e.g. P-256).
     *
     * @param crv the curve name
     */
    public void setCrv(String crv) {
        this.crv = crv;
    }

    /**
     * Returns the base64url encoded x coordinate.
     *
     * @return the x coordinate
     */
    public String getX() {
        return x;
    }

    /**
     * Sets the base64url encoded x coordinate.
     *
     * @param x the x coordinate
     */
    public void setX(String x) {
        this.x = x;
    }

    /**
     * Returns the base64url encoded y coordinate.
     *
     * @return the y coordinate
     */
    public String getY() {
        return y;
    }

    /**
     * Sets the base64url encoded y coordinate.
     *
     * @param y the y coordinate
     */
    public void setY(String y) {
        this.y = y;
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
                Objects.equals(getN(), key.getN()) &&
                Objects.equals(getCrv(), key.getCrv()) &&
                Objects.equals(getX(), key.getX()) &&
                Objects.equals(getY(), key.getY());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAlg(), getKid(), getKty(), getUse(), getE(), getN(), getCrv(), getX(), getY());
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
                ", crv='" + crv + '\'' +
                ", x='" + x + '\'' +
                ", y='" + y + '\'' +
                '}';
    }
}