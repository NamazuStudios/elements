package dev.getelements.elements.sdk.model.health;

import java.util.Objects;

public class DatabaseHealthStatus {

    private String name;

    private String metadata;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DatabaseHealthStatus{");
        sb.append("name='").append(name).append('\'');
        sb.append(", metadata='").append(metadata).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatabaseHealthStatus that = (DatabaseHealthStatus) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getMetadata(), that.getMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getMetadata());
    }

}
