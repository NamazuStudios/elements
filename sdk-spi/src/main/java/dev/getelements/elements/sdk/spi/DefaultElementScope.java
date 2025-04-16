package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.ElementScope;
import dev.getelements.elements.sdk.MutableAttributes;
import dev.getelements.elements.sdk.util.InheritedAttributes;
import dev.getelements.elements.sdk.util.PropertiesAttributes;
import dev.getelements.elements.sdk.util.SimpleAttributes;

import java.util.Properties;

public class DefaultElementScope implements ElementScope {

    private final String name;

    private final MutableAttributes attributes;

    public DefaultElementScope(final String name, final MutableAttributes attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public MutableAttributes getMutableAttributes() {
        return attributes;
    }

    /**
     * Creates a new {@link DefaultElementScope} which inherits its properties from this scope.
     *
     * @return the new {@link DefaultElementScope}
     */
    public DefaultElementScope newInheritedScope(final String name, final MutableAttributes attributes) {
        final var inherited = new InheritedAttributes(this.attributes, attributes);
        return new DefaultElementScope(name, inherited);
    }

    @Override
    public String toString() {
        return "DefaultElementScope{" +
                "name='" + name + '\'' +
                '}';
    }

}
