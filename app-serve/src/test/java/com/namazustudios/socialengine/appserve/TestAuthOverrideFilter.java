package com.namazustudios.socialengine.appserve;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.appserve.guice.AppServeFilterModule;
import com.namazustudios.socialengine.appserve.guice.AppServeSecurityModule;
import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.dao.SessionDao;
import com.namazustudios.socialengine.rt.Response;
import com.namazustudios.socialengine.rt.SimpleRequest;
import com.namazustudios.socialengine.rt.SimpleResponse;
import com.namazustudios.socialengine.rt.guice.RequestScope;
import com.namazustudios.socialengine.rt.handler.Filter;
import com.namazustudios.socialengine.service.ProfileOverrideService;
import com.namazustudios.socialengine.service.SessionService;
import com.namazustudios.socialengine.service.auth.DefaultSessionService;
import com.namazustudios.socialengine.service.profile.ProfileOverrideServiceProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.Constants.SESSION_TIMEOUT_SECONDS;
import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.rt.DummySession.getDummySession;
import static org.mockito.Mockito.mock;

@Guice(modules = {
    AppServeFilterModule.class,
    AppServeSecurityModule.class,
    TestAuthOverrideFilter.Module.class
})
public class TestAuthOverrideFilter {

    private Filter.Chain.Builder builder;

    @Test
    public void testNoOverride() {

        final SimpleRequest request = new SimpleRequest.Builder()
            .header(SESSION_SECRET, "asdf")
            .build();

        final Filter.Chain terminal = (s, r, rr) -> {
            final Response response = SimpleResponse.builder().build();
            rr.accept(response);
        };

        getBuilder().terminate(terminal).next(getDummySession(), request, response -> {

        });

    }

    public Filter.Chain.Builder getBuilder() {
        return builder;
    }

    @Inject
    public void setBuilder(Filter.Chain.Builder builder) {
        this.builder = builder;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {

            bind(ProfileDao.class).toInstance(mock(ProfileDao.class));
            bind(SessionDao.class).toInstance(mock(SessionDao.class));

            bind(SessionService.class)
                .to(DefaultSessionService.class);

            bind(ProfileOverrideService.class)
                .toProvider(ProfileOverrideServiceProvider.class)
                .in(RequestScope.getInstance());

            bind(Long.class).annotatedWith(named(SESSION_TIMEOUT_SECONDS)).toInstance(60l);

        }

    }

}
