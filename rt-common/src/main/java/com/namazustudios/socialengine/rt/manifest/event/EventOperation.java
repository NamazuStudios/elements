package com.namazustudios.socialengine.rt.manifest.event;
import java.util.Objects;

public class EventOperation {

    private String module;

    private String method;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventOperation that = (EventOperation) o;
        return Objects.equals(module, that.module) &&
                Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module, method);
    }
}
