package com.namazustudios.socialengine.rt.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.edge.*;

/**
 * Created by patricktwohig on 9/22/15.
 */
public class SimpleEdgeServerModule extends AbstractModule {

    @Override
    protected void configure() {

        binder().bind(Server.class)
                .to(EdgeServer.class);

        binder().bind(EdgeServer.class)
                .to(SimpleEdgeServer.class);

        binder().bind(SimpleEdgeServer.class)
                .in(Scopes.SINGLETON);

        binder().bind(EdgeRequestDispatcher.class)
                .to(SimpleEdgeRequestDispatcher.class)
                .in(Scopes.SINGLETON);

        binder().bind(new TypeLiteral<ResourceService<EdgeResource>>() {})
                .to(new TypeLiteral<SimpleResourceService<EdgeResource>>() {})
                .in(Scopes.SINGLETON);

        binder().bind(EventService.class)
                .to(SimpleEventService.class)
                .in(Scopes.SINGLETON);

    }
}
