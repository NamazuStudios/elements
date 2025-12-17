package dev.getelements.elements.sdk.service.meta.facebookiap.client.model;

import java.util.Objects;

public class FacebookRef {
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FacebookRef that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "FacebookRef{" +
                "id='" + id + '\'' +
                '}';
    }
}
