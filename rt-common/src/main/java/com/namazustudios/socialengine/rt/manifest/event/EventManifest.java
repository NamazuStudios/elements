package com.namazustudios.socialengine.rt.manifest.event;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EventManifest {

    @NotNull
    private Map<String, @NotNull List<@NotNull EventOperation>> modulesByEventName;

    public Map<String, List<EventOperation>> getModulesByEventName() {
        return modulesByEventName;
    }

    public void setModulesByEventName(Map<String, List<EventOperation>> modulesByName) {
        this.modulesByEventName = modulesByName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventManifest that = (EventManifest) o;
        return Objects.equals(modulesByEventName, that.modulesByEventName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modulesByEventName);
    }
}
