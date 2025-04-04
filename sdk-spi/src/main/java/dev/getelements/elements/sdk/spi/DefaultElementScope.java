package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.ElementScope;
import dev.getelements.elements.sdk.MutableAttributes;
import dev.getelements.elements.sdk.util.PropertiesAttributes;

import java.util.Properties;

public class DefaultElementScope implements ElementScope {

    private final String name;

    private final Properties properties;

    private final PropertiesAttributes attributes;

    public static DefaultElementScope wrap(final String name, final Properties properties) {
        return new DefaultElementScope(name, properties);
    }

    private DefaultElementScope(final String name, final Properties properties) {
        this.name = name;
        this.properties = properties;
        this.attributes = PropertiesAttributes.wrap(properties);
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
    public DefaultElementScope newInheritedScope(final String name, final Properties properties) {
        final var subordinateProperties = new Properties(this.properties);
        subordinateProperties.putAll(properties);
        return new DefaultElementScope(name, subordinateProperties);
    }

    @Override
    public String toString() {
        return "DefaultElementScope{" +
                "name='" + name + '\'' +
                '}';
    }

}
