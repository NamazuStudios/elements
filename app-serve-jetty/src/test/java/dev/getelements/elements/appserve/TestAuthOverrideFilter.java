package dev.getelements.elements.appserve;

import com.google.inject.AbstractModule;
import dev.getelements.elements.appserve.guice.AppServeFilterModule;
import dev.getelements.elements.appserve.guice.AppServeSecurityModule;
import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.dao.SessionDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.rt.Response;
import dev.getelements.elements.rt.SimpleRequest;
import dev.getelements.elements.rt.SimpleResponse;
import dev.getelements.elements.rt.guice.RequestScope;
import dev.getelements.elements.rt.handler.Filter;
import dev.getelements.elements.service.ProfileOverrideService;
import dev.getelements.elements.service.SessionService;
import dev.getelements.elements.service.auth.DefaultSessionService;
import dev.getelements.elements.service.profile.ProfileOverrideServiceProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import java.util.Optional;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.Constants.SESSION_TIMEOUT_SECONDS;
import static dev.getelements.elements.Headers.SESSION_SECRET;
import static dev.getelements.elements.Headers.SOCIALENGINE_SESSION_SECRET;
import static dev.getelements.elements.model.user.User.USER_ATTRIBUTE;
import static dev.getelements.elements.model.session.Session.SESSION_ATTRIBUTE;
import static dev.getelements.elements.rt.DummySession.getDummySession;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.fail;

@Guice(modules = {
    AppServeFilterModule.class,
    AppServeSecurityModule.class,
    TestAuthOverrideFilter.Module.class
})
public class TestAuthOverrideFilter {

    private ProfileDao profileDao;

    private SessionDao sessionDao;

    private Filter.Chain.Builder builder;

    @DataProvider
    public Object[][] getAuthHeader() {
        return new Object[][] {
            new Object[] { SESSION_SECRET },
            new Object[] { SOCIALENGINE_SESSION_SECRET }
        };
    }

    @BeforeMethod
    public void resetMocks() {
        reset(sessionDao, profileDao);
    }

    @Test
    public void testNoSession() {

        final SimpleRequest request = new SimpleRequest.Builder()
            .build();

        final Filter.Chain terminal = (s, r, rr) -> {
            final Response response = SimpleResponse.builder().build();
            rr.accept(response);
        };

        when(getSessionDao().refresh(eq("asdf"), anyLong())).thenThrow(NotFoundException.class);
        getBuilder().terminate(terminal).next(getDummySession(), request, r -> {});

    }

    @Test(dataProvider = "getAuthHeader", expectedExceptions = ForbiddenException.class)
    public void testBogusHeader(final String authHeader) {

        final SimpleRequest request = new SimpleRequest.Builder()
            .header(authHeader, "bogus")
            .build();

        final Filter.Chain terminal = (s, r, rr) -> {
            final Response response = SimpleResponse.builder().build();
            rr.accept(response);
        };

        when(getSessionDao().refresh(eq("bogus"), anyLong())).thenThrow(NotFoundException.class);
        getBuilder().terminate(terminal).next(getDummySession(), request, r -> fail("Request shouldn't process."));

    }

    @Test(dataProvider = "getAuthHeader")
    public void testSessionNoOverrideNoProfile(final String authHeader) {

        final SimpleRequest request = new SimpleRequest.Builder()
                .header(authHeader, "asdf")
                .build();

        final Filter.Chain terminal = (s, r, rr) -> {
            final Response response = SimpleResponse.builder().build();
            rr.accept(response);
        };

        final User mockUser = new User();
        mockUser.setLevel(User.Level.USER);

        final Session mockSession = new Session();
        mockSession.setUser(mockUser);

        when(getSessionDao().refresh(eq("asdf"), anyLong())).thenReturn(mockSession);
        getBuilder().terminate(terminal).next(getDummySession(), request, response -> {

            final User requestUser = request.getAttributes()
                .getAttributeOptional(USER_ATTRIBUTE)
                .map(User.class::cast)
                .get();

            final Session requestSession = request.getAttributes()
                .getAttributeOptional(SESSION_ATTRIBUTE)
                .map(Session.class::cast)
                .get();

            assertSame(mockUser, requestUser);
            assertSame(mockSession, requestSession);

        });

    }

    @Test(dataProvider = "getAuthHeader")
    public void testSessionOverrideProfile(final String authHeader) {

        final SimpleRequest request = new SimpleRequest.Builder()
                .header(authHeader, "foo pbar")
                .build();

        final Filter.Chain terminal = (s, r, rr) -> {
            final Response response = SimpleResponse.builder().build();
            rr.accept(response);
        };

        final User mockUser = new User();
        mockUser.setLevel(User.Level.USER);

        final Session mockSession = new Session();
        final Profile mockProfile = new Profile();
        mockSession.setUser(mockUser);

        when(getSessionDao().refresh(eq("foo"), anyLong())).thenReturn(mockSession);
        when(getProfileDao().findActiveProfile(eq("bar"))).thenReturn(Optional.of(mockProfile));

        getBuilder().terminate(terminal).next(getDummySession(), request, response -> {

            final User requestUser = request.getAttributes()
                    .getAttributeOptional(USER_ATTRIBUTE)
                    .map(User.class::cast)
                    .get();

            final Session requestSession = request.getAttributes()
                    .getAttributeOptional(SESSION_ATTRIBUTE)
                    .map(Session.class::cast)
                    .get();

            assertSame(mockUser, requestUser);
            assertSame(mockSession, requestSession);

        });

    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public SessionDao getSessionDao() {
        return sessionDao;
    }

    @Inject
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
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
