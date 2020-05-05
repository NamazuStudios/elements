package com.namazustudios.socialengine.appserve;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.appserve.guice.AppServeFilterModule;
import com.namazustudios.socialengine.appserve.guice.AppServeSecurityModule;
import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.dao.SessionDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.rt.Response;
import com.namazustudios.socialengine.rt.SimpleRequest;
import com.namazustudios.socialengine.rt.SimpleResponse;
import com.namazustudios.socialengine.rt.guice.RequestScope;
import com.namazustudios.socialengine.rt.handler.Filter;
import com.namazustudios.socialengine.service.ProfileOverrideService;
import com.namazustudios.socialengine.service.SessionService;
import com.namazustudios.socialengine.service.auth.DefaultSessionService;
import com.namazustudios.socialengine.service.profile.ProfileOverrideServiceProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import java.util.Optional;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.Constants.SESSION_TIMEOUT_SECONDS;
import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.model.user.User.USER_ATTRIBUTE;
import static com.namazustudios.socialengine.model.session.Session.SESSION_ATTRIBUTE;
import static com.namazustudios.socialengine.rt.DummySession.getDummySession;
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

    @BeforeMethod
    public void resetMocks() {
        reset(sessionDao, profileDao);
    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void testNoSession() {

        final SimpleRequest request = new SimpleRequest.Builder()
            .header(SESSION_SECRET, "asdf")
            .build();

        final Filter.Chain terminal = (s, r, rr) -> {
            final Response response = SimpleResponse.builder().build();
            rr.accept(response);
        };

        when(getSessionDao().refresh(eq("asdf"), anyLong())).thenThrow(NotFoundException.class);
        getBuilder().terminate(terminal).next(getDummySession(), request, response -> fail("Should never process request."));

    }

    @Test
    public void testSessionNoOverrideNoProfile() {

        final SimpleRequest request = new SimpleRequest.Builder()
                .header(SESSION_SECRET, "asdf")
                .build();

        final Filter.Chain terminal = (s, r, rr) -> {
            final Response response = SimpleResponse.builder().build();
            rr.accept(response);
        };

        final User mockUser = new User();
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

    @Test
    public void testSessionOverrideProfile() {

        final SimpleRequest request = new SimpleRequest.Builder()
                .header(SESSION_SECRET, "foo pbar")
                .build();

        final Filter.Chain terminal = (s, r, rr) -> {
            final Response response = SimpleResponse.builder().build();
            rr.accept(response);
        };

        final User mockUser = new User();
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
