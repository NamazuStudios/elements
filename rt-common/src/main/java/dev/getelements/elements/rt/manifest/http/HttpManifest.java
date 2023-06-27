package dev.getelements.elements.rt.manifest.http;

import dev.getelements.elements.rt.manifest.security.AuthScheme;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * Contains a listing of all {@link HttpOperation}
 *
 * Created by patricktwohig on 8/9/17.
 */
public class HttpManifest implements Serializable {

    @Valid
    @NotNull
    private Map<@NotNull String, @NotNull HttpModule> modulesByName;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HttpManifest)) return false;

        HttpManifest that = (HttpManifest) o;

        return getModulesByName() != null ? getModulesByName().equals(that.getModulesByName()) : that.getModulesByName() == null;
    }

    @Override
    public int hashCode() {
        return getModulesByName() != null ? getModulesByName().hashCode() : 0;
    }

}
