package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.ElementScope;
import dev.getelements.elements.sdk.MutableAttributes;
import dev.getelements.elements.sdk.util.PropertiesAttributes;

import java.util.Properties;

public class DefaultElementScope implements ElementScope {

    private Properties properties;

    private PropertiesAttributes attributes;

    public static DefaultElementScope copyFrom(final Attributes attributes) {
        return new DefaultElementScope(attributes.asProperties());
    }

    private DefaultElementScope(final Properties properties) {
        this.properties = properties;
        this.attributes = PropertiesAttributes.wrap(properties);
    }

    @Override
    public MutableAttributes getAttributes() {
        return attributes;
    }

    /**
     * Creates a new {@link ElementScope} which inherits its properties from this scope.
     *
     * @return the new {@link DefaultElementScope}
     */
    public DefaultElementScope newInheritedScope() {
        final var subordinateProperties = new Properties(properties);
        return new DefaultElementScope(subordinateProperties);
    }

}
