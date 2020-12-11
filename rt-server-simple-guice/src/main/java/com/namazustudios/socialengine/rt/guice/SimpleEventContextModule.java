package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.EventContext;
import com.namazustudios.socialengine.rt.EventService;
import com.namazustudios.socialengine.rt.SimpleEventContext;
import com.namazustudios.socialengine.rt.SimpleEventService;

public class SimpleEventContextModule extends PrivateModule {
    @Override
    protected void configure() {
        expose(EventContext.class);
        bind(EventContext.class).to(SimpleEventContext.class);
        bind(EventService.class).to(SimpleEventService.class);
    }
}
