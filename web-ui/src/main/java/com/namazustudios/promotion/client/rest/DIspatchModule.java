package com.namazustudios.promotion.client.rest;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Scopes;
import com.gwtplatform.dispatch.rest.client.RestApplicationPath;
import com.gwtplatform.dispatch.rest.client.RestDispatch;
import com.gwtplatform.dispatch.rest.client.gin.RestDispatchAsyncModule;

import javax.inject.Singleton;

/**
 * Created by patricktwohig on 5/1/15.
 */
public class DIspatchModule extends AbstractGinModule {

    @Override
    protected void configure() {
        final RestDispatchAsyncModule.Builder builder = new RestDispatchAsyncModule.Builder();
        install(builder.build());
        bindConstant().annotatedWith(RestApplicationPath.class).to("/api");
        bind(LoginResource.class).in(Singleton.class);
    }

}
