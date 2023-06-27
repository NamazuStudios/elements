package dev.getelements.elements.rt.manifest;

import dev.getelements.elements.rt.annotation.DeprecationDefinition;

import java.io.Serializable;
import java.util.Objects;

public class Deprecation implements Serializable {

    private boolean deprecated;

    private String deprecationMessage;

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public String getDeprecationMessage() {
        return deprecationMessage;
    }

    public void setDeprecationMessage(String deprecationMessage) {
        this.deprecationMessage = deprecationMessage;
    }

    public static Deprecation from(final DeprecationDefinition deprecationDefinition) {

        final var deprecation = new Deprecation();

        if (deprecationDefinition.deprecated()) {
            deprecation.setDeprecated(true);
            deprecation.setDeprecationMessage(deprecation.getDeprecationMessage());
        }

        return deprecation;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Deprecation that = (Deprecation) o;
        return isDeprecated() == that.isDeprecated() && Objects.equals(getDeprecationMessage(), that.getDeprecationMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isDeprecated(), getDeprecationMessage());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Deprecation{");
        sb.append("deprecated=").append(deprecated);
        sb.append(", deprecationMessage='").append(deprecationMessage).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
