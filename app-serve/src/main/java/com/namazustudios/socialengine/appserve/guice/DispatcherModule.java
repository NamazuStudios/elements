package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.dao.rt.guice.RTFileAssetLoaderModule;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.guice.ExceptionMapperModule;
import com.namazustudios.socialengine.rt.guice.FilterModule;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolverModule;
import com.namazustudios.socialengine.rt.guice.SimpleContextModule;
import com.namazustudios.socialengine.rt.handler.SessionRequestDispatcher;
import com.namazustudios.socialengine.rt.http.HttpRequest;
import com.namazustudios.socialengine.rt.http.RequestScopedHttpSessionDispatcher;
import com.namazustudios.socialengine.rt.jackson.guice.MultiContentTypeJacksonPayloadWriterModule;
import com.namazustudios.socialengine.rt.jackson.guice.MultiContentTypeJacksonPayloadReaderModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.ClusterClientContextModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQRemoteInvokerModule;
import com.namazustudios.socialengine.rt.servlet.*;
import org.eclipse.jetty.deploy.App;

import java.io.File;

import static com.google.inject.name.Names.named;

public class DispatcherModule extends PrivateModule {

    private final String connectAddress;

    private final File assetRootDirectory;

    public DispatcherModule(final String connectAddress, final File assetRootDirectory) {
        this.connectAddress = connectAddress;
        this.assetRootDirectory = assetRootDirectory;
    }

    @Override
    protected void configure() {

        install(new ExtendedLuaModule());
        install(new GuiceIoCResolverModule());
        install(new RTFileAssetLoaderModule(assetRootDirectory));

        install(new FilterModule());
        install(new ExceptionMapperModule());

        install(new MultiContentTypeJacksonPayloadReaderModule());
        install(new MultiContentTypeJacksonPayloadWriterModule());

        bind(HttpSessionService.class).to(DefaultHttpSessionService.class).asEagerSingleton();
        bind(HttpRequestService.class).to(DefaultHttpRequestService.class);
        bind(HttpResponseService.class).to(DefaultHttpResponseService.class);
        bind(new TypeLiteral<SessionRequestDispatcher<HttpRequest>>(){}).to(RequestScopedHttpSessionDispatcher.class);
        bind(DispatcherServlet.class).in(Scopes.SINGLETON);

        expose(DispatcherServlet.class);

    }

}
