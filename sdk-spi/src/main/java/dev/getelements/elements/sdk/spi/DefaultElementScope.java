package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.ElementScope;
import dev.getelements.elements.sdk.MutableAttributes;
import dev.getelements.elements.sdk.util.InheritedMutableAttributes;
import dev.getelements.elements.sdk.util.SimpleAttributes;

public class DefaultElementScope implements ElementScope {

    private final String name;

    private final InheritedMutableAttributes attributes;

    public DefaultElementScope(final String name, final InheritedMutableAttributes attributes) {
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
    public DefaultElementScope newInheritedScope(final String name, final Attributes attributes) {

        final var inherited = this.attributes
                .newDerivativeFrom(attributes)
                .newDerivativeFrom(SimpleAttributes.newDefaultInstance());

        return new DefaultElementScope(name, inherited);

    }

    @Override
    public String toString() {
        return "DefaultElementScope{" +
                "name='" + name + '\'' +
                '}';
    }

}
