package dev.getelements.elements.sdk.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class JWKSet implements Serializable {

    @Schema(description = "The JWKs")
    private List<JWK> keys;

    public List<JWK> getKeys() {
        return keys;
    }

    public void setKeys(List<JWK> keys) {
        this.keys = keys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JWKSet jwkSet = (JWKSet) o;
        return Objects.equals(getKeys(), jwkSet.getKeys());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKeys());
    }

    @Override
    public String toString() {
        return "JWKSet{" +
                "keys=" + keys +
                '}';
    }

}
