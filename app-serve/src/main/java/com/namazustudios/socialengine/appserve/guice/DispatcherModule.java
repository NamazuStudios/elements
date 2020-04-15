package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.appserve.ProfileOverrideFilter;
import com.namazustudios.socialengine.dao.rt.guice.RTFileAssetLoaderModule;
import com.namazustudios.socialengine.rt.guice.ExceptionMapperModule;
import com.namazustudios.socialengine.rt.guice.FilterModule;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolverModule;
import com.namazustudios.socialengine.rt.handler.SessionRequestDispatcher;
import com.namazustudios.socialengine.rt.http.HttpRequest;
import com.namazustudios.socialengine.rt.http.RequestScopedHttpSessionDispatcher;
import com.namazustudios.socialengine.rt.jackson.guice.MultiContentTypeJacksonPayloadReaderModule;
import com.namazustudios.socialengine.rt.jackson.guice.MultiContentTypeJacksonPayloadWriterModule;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import com.namazustudios.socialengine.rt.servlet.*;

import java.io.File;

public class DispatcherModule extends PrivateModule {

    private final File assetRootDirectory;

    public DispatcherModule(final File assetRootDirectory) {
        this.assetRootDirectory = assetRootDirectory;
    }

    @Override
    protected void configure() {

        install(new LuaModule());
        install(new GuiceIoCResolverModule());
        install(new RTFileAssetLoaderModule(assetRootDirectory));

        install(new FilterModule(){
            @Override
            protected void configureFilters() {
                bindFilter()
                    .named("profileOverride")
                    .atBeginningOfFilterChain()
                    .to(ProfileOverrideFilter.class);
            }
        });

        install(new ExceptionMapperModule());

        install(new MultiContentTypeJacksonPayloadReaderModule());
        install(new MultiContentTypeJacksonPayloadWriterModule());

        bind(HttpSessionService.class).to(DefaultHttpSessionService.class).asEagerSingleton();
        bind(HttpRequestService.class).to(DefaultHttpRequestService.class);
        bind(HttpResponseService.class).to(DefaultHttpResponseService.class);
        bind(new TypeLiteral<SessionRequestDispatcher<HttpRequest>>(){}).to(RequestScopedHttpSessionDispatcher.class);
        bind(DispatcherServlet.class).asEagerSingleton();

        expose(DispatcherServlet.class);

    }

}
