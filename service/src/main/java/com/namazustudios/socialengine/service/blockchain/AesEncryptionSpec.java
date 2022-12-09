package com.namazustudios.socialengine.service.blockchain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;

public class AesEncryptionSpec {

    @NotNull
    @Pattern(regexp = "AES")
    private String family;

    @NotNull
    private String algorithm;

    @NotNull
    private String secretKeyAlgorithm;

    @NotNull
    @Pattern(regexp = "^[a-fA-F0-9]+$")
    private String salt;

    @NotNull
    @Pattern(regexp = "^[a-fA-F0-9]+$")
    private String iv;

    @Min(256)
    private int keyLength;

    @Min(65536)
    private int iterations;

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

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public void setKeyLength(int keyLength) {
        this.keyLength = keyLength;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AesEncryptionSpec that = (AesEncryptionSpec) o;
        return getKeyLength() == that.getKeyLength() && getIterations() == that.getIterations() && Objects.equals(getFamily(), that.getFamily()) && Objects.equals(getAlgorithm(), that.getAlgorithm()) && Objects.equals(getSecretKeyAlgorithm(), that.getSecretKeyAlgorithm()) && Objects.equals(getSalt(), that.getSalt()) && Objects.equals(getIv(), that.getIv());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFamily(), getAlgorithm(), getSecretKeyAlgorithm(), getSalt(), getIv(), getKeyLength(), getIterations());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AesEncryptionSpec{");
        sb.append("family='").append(family).append('\'');
        sb.append(", algorithm='").append(algorithm).append('\'');
        sb.append(", secretKeyAlgorithm='").append(secretKeyAlgorithm).append('\'');
        sb.append(", salt='").append(salt).append('\'');
        sb.append(", iv='").append(iv).append('\'');
        sb.append(", keyLength=").append(keyLength);
        sb.append(", iterations=").append(iterations);
        sb.append('}');
        return sb.toString();
    }

}
