package dev.getelements.elements.service;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.MutableAttributes;
import dev.getelements.elements.sdk.util.ReentrantThreadLocal;
import dev.getelements.elements.sdk.util.SimpleAttributes;
import dev.getelements.elements.sdk.guice.ReentrantThreadLocalScope;

import jakarta.inject.Provider;

public class TestScope {

    private static final ReentrantThreadLocal<Context> currentContext = new ReentrantThreadLocal<>();

    public static ReentrantThreadLocalScope<Context> scope = new ReentrantThreadLocalScope<>(
            Context.class,
            currentContext,
            Context::getAttributes
    );

    public static Context current() {
        return currentContext.getCurrent();
    }

    public static Context enter() {
        return new Context() {

            private final MutableAttributes attributes = new SimpleAttributes.Builder().build();

            private final ReentrantThreadLocal.Scope<Context> underlying = currentContext.enter(this);

            @Override
            public MutableAttributes getAttributes() {
                return attributes;
            }

            @Override
            public void close() {
                underlying.close();
            }

        };
    }

    public interface Context extends AutoCloseable {

        MutableAttributes getAttributes();

        void close();

    }

    public static class AttributesProvider implements Provider<Attributes> {

        @Override
        public Attributes get() {
            return currentContext.getCurrent().getAttributes();
        }

    }

}
