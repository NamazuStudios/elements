package com.namazustudios.socialengine.rt.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.internal.*;

/**
 * Created by patricktwohig on 9/22/15.
 */
public class SimpleInternalServerModule extends AbstractModule{
    @Override
    protected void configure() {

        binder().bind(new TypeLiteral<Container<InternalResource>>(){})
                .to(SimpleInternalContainer.class);

        binder().bind(SimpleInternalContainer.class)
                .in(Scopes.SINGLETON);

        binder().bind(InternalRequestDispatcher.class)
                .to(SimpleInternalRequestDispatcher.class);

        binder().bind(SimpleInternalRequestDispatcher.class)
                .in(Scopes.SINGLETON);

        binder().bind(new TypeLiteral<ResourceService<InternalResource>>() {})
                .to(new TypeLiteral<SimpleResourceService<InternalResource>>() {})
                .in(Scopes.SINGLETON);

        binder().bind(EventService.class)
                .to(SimpleEventService.class)
                .in(Scopes.SINGLETON);

    }
}
