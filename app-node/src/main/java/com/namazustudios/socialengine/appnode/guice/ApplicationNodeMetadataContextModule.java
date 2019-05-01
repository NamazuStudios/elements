package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.*;
import com.namazustudios.socialengine.rt.ApplicationNodeMetadataContext;
import com.namazustudios.socialengine.rt.SimpleApplicationNodeMetadataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationNodeMetadataContextModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationNodeMetadataContextModule.class);

    @Override
    protected void configure() {
        bind(ApplicationNodeMetadataContext.class).to(SimpleApplicationNodeMetadataContext.class);
    }

}
