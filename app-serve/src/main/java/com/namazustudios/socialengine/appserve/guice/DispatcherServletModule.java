package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.guice.ExceptionMapperModule;
import com.namazustudios.socialengine.rt.guice.SimpleServicesModule;
import com.namazustudios.socialengine.rt.handler.SessionRequestDispatcher;
import com.namazustudios.socialengine.rt.http.HttpRequest;
import com.namazustudios.socialengine.rt.http.RequestScopedHttpSessionDispatcher;
import com.namazustudios.socialengine.rt.jackson.guice.JacksonPaylaodWriterModule;
import com.namazustudios.socialengine.rt.jackson.guice.JacksonPayloadReaderModule;
import com.namazustudios.socialengine.rt.servlet.*;

public class DispatcherServletModule extends PrivateModule {

    @Override
    protected void configure() {

        install(new ExceptionMapperModule());
        install(new SimpleServicesModule());
        install(new JacksonPayloadReaderModule());
        install(new JacksonPaylaodWriterModule());

        bind(HttpSessionService.class).to(DefaultHttpSessionService.class);
        bind(HttpRequestService.class).to(DefaultHttpRequestService.class);
        bind(HttpResponseService.class).to(DefaultHttpResponseService.class);
        bind(new TypeLiteral<SessionRequestDispatcher<HttpRequest>>(){}).to(RequestScopedHttpSessionDispatcher.class);
        bind(DispatcherServlet.class).in(Scopes.SINGLETON);

        expose(DispatcherServlet.class);

    }

}
