package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.ElementScope;
import dev.getelements.elements.sdk.util.ReentrantThreadLocal;

import java.util.Properties;

public class DefaultElementScopeBuilder implements ElementScope.Builder {

    private String name = ElementScope.ANONYMOUS;

    private final Properties properties;

    private final ReentrantThreadLocal<DefaultElementScope> reentrantThreadLocal;

    public DefaultElementScopeBuilder(
            final Properties properties,
            final ReentrantThreadLocal<DefaultElementScope> reentrantThreadLocal) {
        this.properties = new Properties(properties);
        this.reentrantThreadLocal = reentrantThreadLocal;
    }

    @Override
    public ElementScope.Builder named(final String name) {
        this.name = name == null ? ElementScope.ANONYMOUS : name;
        return this;
    }

    @Override
    public ElementScope.Handle enter() {

        final var newScope = reentrantThreadLocal
                .getCurrentOptional()
                .map(des -> des.newInheritedScope(name, properties))
                .orElseGet(() -> DefaultElementScope.wrap(name, properties));

        return reentrantThreadLocal.enter(newScope)::close;

    }

    @Override
    public <T> ElementScope.Builder with(final String name, final T object) {
        properties.put(name, object);
        return this;
    }

}
