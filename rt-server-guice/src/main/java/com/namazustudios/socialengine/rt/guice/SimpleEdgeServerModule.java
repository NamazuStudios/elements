package com.namazustudios.socialengine.rt.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.namazustudios.socialengine.rt.EventService;
import com.namazustudios.socialengine.rt.SimpleEventService;

/**
 * Created by patricktwohig on 9/22/15.
 */
public class SimpleEdgeServerModule extends AbstractModule {

    @Override
    protected void configure() {

//        binder().bind(new TypeLiteral<Scheduler<Handler>>(){})
//                .to(SimpleScheduler.class)
//                .in(Scopes.SINGLETON);
//
////        binder().bind(SessionRequestDispatcher.class)
////                .to(SimpleSessionRequestDispatcher.class)
////                .in(Scopes.SINGLETON);
//
//        binder().bind(new TypeLiteral<ResourceService<Handler>>() {})
//                .to(new TypeLiteral<SimpleResourceService<Handler>>() {})
//                .in(Scopes.SINGLETON);

        binder().bind(EventService.class)
                .to(SimpleEventService.class)
                .in(Scopes.SINGLETON);

    }
}
