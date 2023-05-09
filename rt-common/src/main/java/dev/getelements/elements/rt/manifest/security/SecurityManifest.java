package dev.getelements.elements.rt.manifest.security;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

public class SecurityManifest implements Serializable {

    @NotNull
    private Map<String, AuthScheme.Header> headerAuthSchemesByName;

    /**
     * Returns a mapping of header authorization schemes by name.
     *
     * @return the mapping of header auth schemes by name.
     */
    public Map<String, AuthScheme.Header> getHeaderAuthSchemesByName() {
        return headerAuthSchemesByName;
    }

    /**
     * Sets a mapping of header authorization schemes by name.
     *
     * @param headerAuthSchemesByName the mapping of header auth schemes by name.
     */
    public void setHeaderAuthSchemesByName(Map<String, AuthScheme.Header> headerAuthSchemesByName) {
        this.headerAuthSchemesByName = headerAuthSchemesByName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SecurityManifest)) return false;

        SecurityManifest that = (SecurityManifest) o;

        return getHeaderAuthSchemesByName() != null ? getHeaderAuthSchemesByName().equals(that.getHeaderAuthSchemesByName()) : that.getHeaderAuthSchemesByName() == null;
    }

    @Override
    public int hashCode() {
        return getHeaderAuthSchemesByName() != null ? getHeaderAuthSchemesByName().hashCode() : 0;
    }

}
