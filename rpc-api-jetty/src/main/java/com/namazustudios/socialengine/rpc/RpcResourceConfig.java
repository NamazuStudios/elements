package com.namazustudios.socialengine.rpc;

import com.namazustudios.socialengine.rt.DefaultExceptionMapper;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Inject;
import javax.servlet.ServletContext;

public class RpcResourceConfig extends ResourceConfig {

    public static final String INJECTOR_ATTRIBUTE_NAME = RpcResourceConfig.class.getName() + ".Injector";

    @Inject
    public RpcResourceConfig(final ServiceLocator serviceLocator, final ServletContext context) {

        register(SwaggerSerializers.class);
        register(DefaultExceptionMapper.class);

    }

}
