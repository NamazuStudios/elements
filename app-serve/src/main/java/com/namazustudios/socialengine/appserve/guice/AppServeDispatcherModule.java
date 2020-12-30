package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.appserve.ServiceExceptionMapper;
import com.namazustudios.socialengine.rt.guice.ExceptionMapperModule;
import com.namazustudios.socialengine.rt.handler.SessionRequestDispatcher;
import com.namazustudios.socialengine.rt.http.HttpRequest;
import com.namazustudios.socialengine.rt.http.RequestScopedHttpSessionDispatcher;
import com.namazustudios.socialengine.rt.jackson.guice.MultiContentTypeJacksonPayloadReaderModule;
import com.namazustudios.socialengine.rt.jackson.guice.MultiContentTypeJacksonPayloadWriterModule;
import com.namazustudios.socialengine.rt.remote.ContextLocalInvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.LocalInvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.RemoteInvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.SimpleRemoteInvocationDispatcher;
import com.namazustudios.socialengine.rt.servlet.*;

public class AppServeDispatcherModule extends PrivateModule {

    @Override
    protected void configure() {

        install(new ExceptionMapperModule() {
            @Override
            protected void configureExceptionMappers() {
                bind(ServiceExceptionMapper.class);
            }
        });

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
