package com.namazustudios.socialengine.rt.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.SimpleResourceService;
import com.namazustudios.socialengine.rt.edge.*;
import com.namazustudios.socialengine.rt.internal.InternalResource;
import com.namazustudios.socialengine.rt.internal.InternalServer;
import com.namazustudios.socialengine.rt.internal.SimpleInternalServer;

/**
 * Created by patricktwohig on 9/2/15.
 */
public class SimpleServerModule extends AbstractModule {

    @Override
    protected void configure() {

        binder().bind(EdgeServer.class)
                .to(SimpleEdgeServer.class)
                .in(Scopes.SINGLETON);

        binder().bind(EdgeRequestDispatcher.class)
                .to(SimpleEdgeRequestDispatcher.class)
                .in(Scopes.SINGLETON);

        binder().bind(new TypeLiteral<ResourceService<EdgeResource>>(){})
                .to(new TypeLiteral<SimpleResourceService<EdgeResource>>(){})
                .in(Scopes.SINGLETON);

        binder().bind(InternalServer.class)
                .to(SimpleInternalServer.class)
                .in(Scopes.SINGLETON);

        binder().bind(new TypeLiteral<ResourceService<InternalResource>>(){})
                .to(new TypeLiteral<SimpleResourceService<InternalResource>>(){})
                .in(Scopes.SINGLETON);

    }

}
