package dev.getelements.elements.rt.manifest.jrpc;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class JsonRpcManifest implements Serializable {

    @Valid
    @NotNull
    private Map<@NotNull String, @NotNull JsonRpcService> servicesByName;

    public Map<String, JsonRpcService> getServicesByName() {
        return servicesByName;
    }

    public void setServicesByName(Map<String, JsonRpcService> servicesByName) {
        this.servicesByName = servicesByName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonRpcManifest that = (JsonRpcManifest) o;
        return Objects.equals(getServicesByName(), that.getServicesByName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServicesByName());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JsonRpcManifest{");
        sb.append("modulesByName=").append(servicesByName);
        sb.append('}');
        return sb.toString();
    }

}
