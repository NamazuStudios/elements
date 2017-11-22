package com.namazustudios.socialengine.rt.manifest.security;

import com.namazustudios.socialengine.rt.manifest.http.HttpOperation;
import com.sun.org.apache.xpath.internal.operations.String;

/**
 * Base class for the various auth schemes.
 */
public abstract class AuthScheme {

    private String name;

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
     * Represents an {@link AuthScheme} which uses a {@link com.namazustudios.socialengine.rt.manifest.Header} to provide credentials.
     */
    public static class Header extends AuthScheme {

        private com.namazustudios.socialengine.rt.manifest.Header header;

        /**
         * The {@link com.namazustudios.socialengine.rt.manifest.Header} used to specify the auth information
         *
         * @return the {@link com.namazustudios.socialengine.rt.manifest.Header} used to specify this auth scheme.
         */
        public com.namazustudios.socialengine.rt.manifest.Header getHeader() {
            return header;
        }

        /**
         * The {@link com.namazustudios.socialengine.rt.manifest.Header} used to specify the auth information
         *
         * @param header  the {@link com.namazustudios.socialengine.rt.manifest.Header} used to specify this auth scheme.
         */
        public void setHeader(com.namazustudios.socialengine.rt.manifest.Header header) {
            this.header = header;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Header)) return false;
            if (!super.equals(o)) return false;

            Header header1 = (Header) o;

            return getHeader() != null ? getHeader().equals(header1.getHeader()) : header1.getHeader() == null;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (getHeader() != null ? getHeader().hashCode() : 0);
            return result;
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
