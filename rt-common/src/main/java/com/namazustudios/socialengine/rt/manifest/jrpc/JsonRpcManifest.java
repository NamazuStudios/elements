package com.namazustudios.socialengine.rt.manifest.jrpc;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class JsonRpcManifest implements Serializable {

    @NotNull
    private Map<@NotNull String, @NotNull JsonRpcService> modulesByName;

    public Map<String, JsonRpcService> getModulesByName() {
        return modulesByName;
    }

    public void setModulesByName(Map<String, JsonRpcService> modulesByName) {
        this.modulesByName = modulesByName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonRpcManifest that = (JsonRpcManifest) o;
        return Objects.equals(getModulesByName(), that.getModulesByName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getModulesByName());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JsonRpcManifest{");
        sb.append("modulesByName=").append(modulesByName);
        sb.append('}');
        return sb.toString();
    }

}
