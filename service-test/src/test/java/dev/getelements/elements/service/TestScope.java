package dev.getelements.elements.service;

import dev.getelements.elements.rt.Attributes;
import dev.getelements.elements.rt.MutableAttributes;
import dev.getelements.elements.rt.ReentrantThreadLocal;
import dev.getelements.elements.rt.SimpleAttributes;
import dev.getelements.elements.rt.guice.ReentrantThreadLocalScope;

import javax.inject.Provider;

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

            private final MutableAttributes attributes = new SimpleAttributes();

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
