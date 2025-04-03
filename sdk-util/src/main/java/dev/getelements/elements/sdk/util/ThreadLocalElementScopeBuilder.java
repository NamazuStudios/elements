package dev.getelements.elements.sdk.util;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.ElementScope;
import dev.getelements.elements.sdk.MutableAttributes;

public class ThreadLocalElementScopeBuilder implements ElementScope.Builder {

    private final MutableAttributes mutableAttributes;

    private final ReentrantThreadLocal<ElementScope> reentrantThreadLocal;

    public ThreadLocalElementScopeBuilder(
            final MutableAttributes mutableAttributes,
            final ReentrantThreadLocal<ElementScope> reentrantThreadLocal) {
        this.mutableAttributes = mutableAttributes;
        this.reentrantThreadLocal = reentrantThreadLocal;
    }

    @Override
    public <T> ElementScope.Builder with(final String name, final T object) {
        mutableAttributes.setAttribute(name, object);
        return this;
    }

    @Override
    public ElementScope build() {
        return new ElementScope() {

            final Attributes attributes = mutableAttributes.immutableCopy();

            final ReentrantThreadLocal.Scope<ElementScope> tls = reentrantThreadLocal.enter(this);

            @Override
            public Attributes getAttributes() {
                return attributes;
            }

            @Override
            public void close() {
                tls.close();
            }

        };

    }

}
