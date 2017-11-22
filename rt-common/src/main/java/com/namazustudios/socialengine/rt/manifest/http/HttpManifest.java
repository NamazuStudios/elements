package com.namazustudios.socialengine.rt.manifest.http;

import java.util.Map;

/**
 * Contains a listing of all {@link HttpOperation}
 *
 * Created by patricktwohig on 8/9/17.
 */
public class HttpManifest {

    private Map<String, HttpModule> modulesByName;

    private Map<String, AuthScheme.Header> headerAuthSchemesByName;

    /**
     * Gets a mapping of {@link HttpModule} instances by their associated name.
     *
     * @return the mapping of {@link HttpModule} by name
     */
    public Map<String, HttpModule> getModulesByName() {
        return modulesByName;
    }

    /**
     * Sets a mapping of {@link HttpModule} instances by their associated name.
     *
     * @param modulesByName the mapping of {@link HttpModule} by name
     */
    public void setModulesByName(Map<String, HttpModule> modulesByName) {
        this.modulesByName = modulesByName;
    }

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
        if (!(o instanceof HttpManifest)) return false;

        HttpManifest that = (HttpManifest) o;

        if (getModulesByName() != null ? !getModulesByName().equals(that.getModulesByName()) : that.getModulesByName() != null)
            return false;
        return getHeaderAuthSchemesByName() != null ? getHeaderAuthSchemesByName().equals(that.getHeaderAuthSchemesByName()) : that.getHeaderAuthSchemesByName() == null;
    }

    @Override
    public int hashCode() {
        int result = getModulesByName() != null ? getModulesByName().hashCode() : 0;
        result = 31 * result + (getHeaderAuthSchemesByName() != null ? getHeaderAuthSchemesByName().hashCode() : 0);
        return result;
    }

}
