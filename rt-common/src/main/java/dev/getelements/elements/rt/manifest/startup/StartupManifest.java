package dev.getelements.elements.rt.manifest.startup;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * Contains a listing of all {@link StartupOperation}
 *
 */
public class StartupManifest {

    @NotNull
    private Map<String, StartupModule> modulesByName;

    /**
     * Gets a mapping of {@link StartupModule} instances by their associated name.
     *
     * @return the mapping of {@link StartupModule} by name
     */
    public Map<String, StartupModule> getModulesByName() {
        return modulesByName;
    }

    /**
     * Sets a mapping of {@link StartupModule} instances by their associated name.
     *
     * @param modulesByName the mapping of {@link StartupModule} by name
     */
    public void setModulesByName(Map<String, StartupModule> modulesByName) {
        this.modulesByName = modulesByName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StartupManifest)) return false;

        StartupManifest that = (StartupManifest) o;

        return getModulesByName() != null ? getModulesByName().equals(that.getModulesByName()) : that.getModulesByName() == null;
    }

    @Override
    public int hashCode() {
        return getModulesByName() != null ? getModulesByName().hashCode() : 0;
    }

}
