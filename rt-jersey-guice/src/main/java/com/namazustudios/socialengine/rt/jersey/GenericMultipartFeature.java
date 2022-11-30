package com.namazustudios.socialengine.rt.jersey;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

public class GenericMultipartFeature implements Feature {

    public static final String TYPE = "type";

    public static final String DISPOSITION = "disposition";

    public static final String ENTITY = "entity";

    public static final String READER = "reader";

    @Override
    public boolean configure(final FeatureContext context) {
        context.register(GenericMultipartReader.class);
        context.register(GenericMultipartWriter.class);
        return true;
    }

}
