package com.namazustudios.socialengine.rt.manifest.security;

import com.namazustudios.socialengine.rt.manifest.Header;
import com.namazustudios.socialengine.rt.manifest.http.HttpOperation;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Base class for the various auth schemes.
 */
public abstract class AuthScheme<SpecT> implements Serializable {

    @NotNull
    private String name;

    @NotNull
    private String description;

    /**
     * The name of the scheme.  This is used as the reference for the auth scheme in the {@link HttpOperation} object.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the auth scheme.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Provides a description for this {@link AuthScheme}.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Provides a description for this {@link AuthScheme}.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the specification for the auth scheme.
     *
     * @return the specification
     */
    public abstract SpecT getSpec();

    /**
     * Sets the specification for the auth scheme.
     *
     * @param spec the specification
     */
    public abstract void setSpec(SpecT spec);

    /**
     * Represents an {@link AuthScheme} which uses a {@link com.namazustudios.socialengine.rt.manifest.Header} to provide credentials.
     */
    public static class Header extends AuthScheme<com.namazustudios.socialengine.rt.manifest.Header> {

        private com.namazustudios.socialengine.rt.manifest.Header spec;

        @Override
        public com.namazustudios.socialengine.rt.manifest.Header getSpec() {
            return spec;
        }

        @Override
        public void setSpec(com.namazustudios.socialengine.rt.manifest.Header spec) {
            this.spec = spec;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthScheme)) return false;

        AuthScheme that = (AuthScheme) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        return getDescription() != null ? getDescription().equals(that.getDescription()) : that.getDescription() == null;
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        return result;
    }
}
