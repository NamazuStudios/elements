package com.namazustudios.socialengine.service.guice;


import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.rt.MutableAttributes;
import com.namazustudios.socialengine.rt.ReentrantThreadLocal;
import com.namazustudios.socialengine.rt.SimpleAttributes;
import com.namazustudios.socialengine.rt.guice.ReentrantThreadLocalScope;

import javax.inject.Provider;

public class TestScope {

    private static final ReentrantThreadLocal<Context> currentContext = new ReentrantThreadLocal<>();

    public static ReentrantThreadLocalScope<Context> scope = new ReentrantThreadLocalScope<>(
            Context.class,
            currentContext,
            Context::getAttributes
    );

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
