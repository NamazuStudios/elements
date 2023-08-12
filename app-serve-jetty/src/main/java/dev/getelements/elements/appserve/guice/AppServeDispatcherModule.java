package dev.getelements.elements.appserve.guice;

import com.google.inject.AbstractModule;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.appserve.ServiceExceptionMapper;
import dev.getelements.elements.rt.guice.ExceptionMapperModule;
import dev.getelements.elements.rt.handler.SessionRequestDispatcher;
import dev.getelements.elements.rt.http.HttpRequest;
import dev.getelements.elements.rt.http.RequestScopedHttpSessionDispatcher;
import dev.getelements.elements.rt.jackson.guice.MultiContentTypeJacksonPayloadReaderModule;
import dev.getelements.elements.rt.jackson.guice.MultiContentTypeJacksonPayloadWriterModule;
import dev.getelements.elements.rt.servlet.*;

public class AppServeDispatcherModule extends AbstractModule {

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

    }

}
