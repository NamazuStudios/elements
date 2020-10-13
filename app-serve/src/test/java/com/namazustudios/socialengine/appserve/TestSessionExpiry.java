package com.namazustudios.socialengine.appserve;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.namazustudios.socialengine.appserve.guice.AppServeFilterModule;
import com.namazustudios.socialengine.appserve.guice.AppServeSecurityModule;
import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.security.SessionExpiredException;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.RequestHeader;
import com.namazustudios.socialengine.rt.SimpleAttributes;
import com.namazustudios.socialengine.rt.handler.Filter;
import com.namazustudios.socialengine.rt.handler.Session;
import com.namazustudios.socialengine.service.ProfileOverrideService;
import com.namazustudios.socialengine.service.SessionService;
import com.namazustudios.socialengine.service.profile.ProfileOverrideServiceProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static com.google.inject.Guice.createInjector;
import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.Headers.SOCIALENGINE_SESSION_SECRET;
import static com.namazustudios.socialengine.model.user.User.Level.USER;
import static java.lang.System.currentTimeMillis;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

public class TestSessionExpiry {

    @Test
    public void testFilterHappy() {

        final Injector injector = createInjector(
            new MockModule(),
            new AppServeFilterModule(),
            new AppServeSecurityModule()
        );

        final Filter.Chain.Builder builder = injector.getInstance(Filter.Chain.Builder.class);

        final Session session = mock(Session.class);
        final Request request = mock(Request.class);
        final RequestHeader header = mock(RequestHeader.class);
        final SimpleAttributes attributes = new SimpleAttributes.Builder().build();

        when(request.getHeader()).thenReturn(header);
        when(request.getAttributes()).thenReturn(attributes);
        when(header.getHeader(SESSION_SECRET)).thenReturn(Optional.of("expired.session.secret"));
        when(header.getHeader(SOCIALENGINE_SESSION_SECRET)).thenReturn(Optional.of("expired.session.secret"));

        final User userModel = new User();
        final Profile profileModel = new Profile();
        final Application applicationModel = new Application();
        final com.namazustudios.socialengine.model.session.Session sessionModel = new com.namazustudios.socialengine.model.session.Session();

        userModel.setLevel(USER);
        sessionModel.setUser(userModel);
        sessionModel.setProfile(profileModel);
        sessionModel.setApplication(applicationModel);
        sessionModel.setExpiry(currentTimeMillis());

        final SessionService sessionService = injector.getInstance(SessionService.class);
        when(sessionService.checkAndRefreshSessionIfNecessary(eq("expired.session.secret")))
            .thenReturn(sessionModel);

        builder.terminate((s, r, rr) -> {
            assertNotNull(s, "Session must not be null.");
            assertNotNull(r, "Request must not be null.");
            assertNotNull(rr, "Response receiver must not be null.");
        }).next(session, request, rr -> {});

    }

    @Test(expectedExceptions = SessionExpiredException.class)
    public void testSessionExpired() {

        final Injector injector = createInjector(
                new MockModule(),
                new AppServeFilterModule(),
                new AppServeSecurityModule()
        );

        final Filter.Chain.Builder builder = injector.getInstance(Filter.Chain.Builder.class);

        final Session session = mock(Session.class);
        final Request request = mock(Request.class);
        final RequestHeader header = mock(RequestHeader.class);
        final SimpleAttributes attributes = new SimpleAttributes.Builder().build();

        when(request.getHeader()).thenReturn(header);
        when(request.getAttributes()).thenReturn(attributes);
        when(header.getHeader(SESSION_SECRET)).thenReturn(Optional.of("expired.session.secret"));
        when(header.getHeader(SOCIALENGINE_SESSION_SECRET)).thenReturn(Optional.of("expired.session.secret"));

        final User userModel = new User();
        final Profile profileModel = new Profile();
        final Application applicationModel = new Application();
        final com.namazustudios.socialengine.model.session.Session sessionModel = new com.namazustudios.socialengine.model.session.Session();

        userModel.setLevel(USER);
        sessionModel.setUser(userModel);
        sessionModel.setProfile(profileModel);
        sessionModel.setApplication(applicationModel);
        sessionModel.setExpiry(currentTimeMillis());

        final SessionService sessionService = injector.getInstance(SessionService.class);
        when(sessionService.checkAndRefreshSessionIfNecessary(eq("expired.session.secret"))).thenAnswer(i -> {
                throw new SessionExpiredException();
            });

        builder.terminate((s, r, rr) -> {
            assertNotNull(s, "Session must not be null.");
            assertNotNull(r, "Request must not be null.");
            assertNotNull(rr, "Response receiver must not be null.");
        }).next(session, request, rr -> {});

    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void testSessionForbidden() {

        final Injector injector = createInjector(
                new MockModule(),
                new AppServeFilterModule(),
                new AppServeSecurityModule()
        );

        final Filter.Chain.Builder builder = injector.getInstance(Filter.Chain.Builder.class);

        final Session session = mock(Session.class);
        final Request request = mock(Request.class);
        final RequestHeader header = mock(RequestHeader.class);
        final SimpleAttributes attributes = new SimpleAttributes.Builder().build();

        when(request.getHeader()).thenReturn(header);
        when(request.getAttributes()).thenReturn(attributes);
        when(header.getHeader(SESSION_SECRET)).thenReturn(Optional.of("expired.session.secret"));
        when(header.getHeader(SOCIALENGINE_SESSION_SECRET)).thenReturn(Optional.of("expired.session.secret"));

        final User userModel = new User();
        final Profile profileModel = new Profile();
        final Application applicationModel = new Application();
        final com.namazustudios.socialengine.model.session.Session sessionModel = new com.namazustudios.socialengine.model.session.Session();

        userModel.setLevel(USER);
        sessionModel.setUser(userModel);
        sessionModel.setProfile(profileModel);
        sessionModel.setApplication(applicationModel);
        sessionModel.setExpiry(currentTimeMillis());

        final SessionService sessionService = injector.getInstance(SessionService.class);
        when(sessionService.checkAndRefreshSessionIfNecessary(eq("expired.session.secret"))).thenAnswer(i -> {
            throw new ForbiddenException();
        });

        builder.terminate((s, r, rr) -> {
            assertNotNull(s, "Session must not be null.");
            assertNotNull(r, "Request must not be null.");
            assertNotNull(rr, "Response receiver must not be null.");
        }).next(session, request, rr -> {});

    }

    public static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ProfileDao.class).toInstance(mock(ProfileDao.class));
            bind(SessionService.class).toInstance(mock(SessionService.class));
            bind(ProfileOverrideService.class).toProvider(ProfileOverrideServiceProvider.class);
        }
    }

}
