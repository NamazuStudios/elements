package com.namazustudios.socialengine.model.applesignin;

import com.namazustudios.socialengine.rt.annotation.ClientSerializationStrategy;

import java.util.List;
import java.util.Objects;

import static com.namazustudios.socialengine.rt.annotation.ClientSerializationStrategy.APPLE_ITUNES;

@ClientSerializationStrategy(APPLE_ITUNES)
public class JWKSet {

    private List<Keys> keys;

    public List<Keys> getKeys() {
        return keys;
    }

    public void setKeys(List<Keys> keys) {
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

    @ClientSerializationStrategy(APPLE_ITUNES)
    public static class Keys {

        private String alg;

        private String kid;

        private String kty;

        private String use;

        private String e;

        private String n;

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
            Keys keys = (Keys) o;
            return Objects.equals(getAlg(), keys.getAlg()) &&
                    Objects.equals(getKid(), keys.getKid()) &&
                    Objects.equals(getKty(), keys.getKty()) &&
                    Objects.equals(getUse(), keys.getUse()) &&
                    Objects.equals(getE(), keys.getE()) &&
                    Objects.equals(getN(), keys.getN());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getAlg(), getKid(), getKty(), getUse(), getE(), getN());
        }

        @Override
        public String toString() {
            return "Keys{" +
                    "alg='" + alg + '\'' +
                    ", kid='" + kid + '\'' +
                    ", kty='" + kty + '\'' +
                    ", use='" + use + '\'' +
                    ", e='" + e + '\'' +
                    ", n='" + n + '\'' +
                    '}';
        }
    }

}
