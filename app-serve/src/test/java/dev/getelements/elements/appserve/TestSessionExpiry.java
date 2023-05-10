package dev.getelements.elements.appserve;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import dev.getelements.elements.appserve.guice.AppServeFilterModule;
import dev.getelements.elements.appserve.guice.AppServeSecurityModule;
import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.exception.security.SessionExpiredException;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.rt.Request;
import dev.getelements.elements.rt.RequestHeader;
import dev.getelements.elements.rt.SimpleAttributes;
import dev.getelements.elements.rt.handler.Filter;
import dev.getelements.elements.rt.handler.Session;
import dev.getelements.elements.service.ProfileOverrideService;
import dev.getelements.elements.service.SessionService;
import dev.getelements.elements.service.profile.ProfileOverrideServiceProvider;
import org.testng.annotations.Test;

import java.util.Optional;

import static com.google.inject.Guice.createInjector;
import static dev.getelements.elements.Headers.SESSION_SECRET;
import static dev.getelements.elements.Headers.SOCIALENGINE_SESSION_SECRET;
import static dev.getelements.elements.model.user.User.Level.USER;
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
        final dev.getelements.elements.model.session.Session sessionModel = new dev.getelements.elements.model.session.Session();

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
        final dev.getelements.elements.model.session.Session sessionModel = new dev.getelements.elements.model.session.Session();

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
        final dev.getelements.elements.model.session.Session sessionModel = new dev.getelements.elements.model.session.Session();

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
