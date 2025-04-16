package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.ElementScope;
import dev.getelements.elements.sdk.util.InheritedAttributes;
import dev.getelements.elements.sdk.util.ReentrantThreadLocal;
import dev.getelements.elements.sdk.util.SimpleAttributes;

import static dev.getelements.elements.sdk.Attributes.emptyAttributes;

public class DefaultElementScopeBuilder implements ElementScope.Builder {

    private String name = ElementScope.ANONYMOUS;

    private Attributes attributes = emptyAttributes();

    private final Attributes base;

    private final ReentrantThreadLocal<DefaultElementScope> reentrantThreadLocal;

    public DefaultElementScopeBuilder(
            final Attributes base,
            final ReentrantThreadLocal<DefaultElementScope> reentrantThreadLocal) {
        this.base = base;
        this.reentrantThreadLocal = reentrantThreadLocal;
    }

    @Override
    public ElementScope.Builder named(final String name) {
        this.name = name == null ? ElementScope.ANONYMOUS : name;
        return this;
    }

    @Override
    public ElementScope.Builder with(final Attributes attributes) {
        this.attributes = attributes == null ? emptyAttributes() : attributes;
        return this;
    }

    @Override
    public ElementScope.Handle enter() {

        final var newScope = reentrantThreadLocal
                .getCurrentOptional()
                .map(des -> des.newInheritedScope(name, attributes))
                .orElseGet(() -> new DefaultElementScope(
                        name,
                        InheritedAttributes
                            .withAttributes(base)
                            .newDerivativeFrom(attributes)
                            .newDerivativeFrom(SimpleAttributes.newDefaultInstance()))
                );

        return reentrantThreadLocal.enter(newScope)::close;

    }

}
