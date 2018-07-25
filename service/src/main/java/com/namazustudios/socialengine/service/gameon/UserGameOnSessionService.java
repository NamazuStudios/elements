package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.dao.GameOnApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.GameOnRegistrationDao;
import com.namazustudios.socialengine.dao.GameOnSessionDao;
import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.exception.ProfileNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.DeviceOSType;
import com.namazustudios.socialengine.model.gameon.GameOnRegistration;
import com.namazustudios.socialengine.model.gameon.GameOnSession;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.GameOnSessionService;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnAuthenticationInvoker;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Objects;
import java.util.function.Supplier;

public class UserGameOnSessionService implements GameOnSessionService {

    private User user;

    private Supplier<Profile> currentProfileSupplier;

    private ProfileDao profileDao;

    private GameOnSessionDao gameOnSessionDao;

    private GameOnRegistrationDao gameOnRegistrationDao;

    private GameOnApplicationConfigurationDao gameOnApplicationConfigurationDao;

    private Provider<GameOnAuthenticationInvoker.Builder> gameOnAuthenticationInvokerBuilderProvider;

    @Override
    public Pagination<GameOnSession> getGameOnSessions(final int offset, final int count) {
        return getGameOnSessionDao().getSessionsForUser(getUser(), offset, count);
    }

    @Override
    public Pagination<GameOnSession> getGameOnSessions(final int offset, final int count, final String search) {
        return getGameOnSessionDao().getSessionsForUser(getUser(), offset, count, search);
    }

    @Override
    public GameOnSession getGameOnSession(final String gameOnSessionId) {
        return getGameOnSessionDao().getSessionForUser(getUser(), gameOnSessionId);
    }

    @Override
    public GameOnSession getCurrentGameOnSession(final DeviceOSType deviceOSType) {
        final Profile profile = getCurrentProfileSupplier().get();
        return getGameOnSessionDao().getSessionForProfile(profile, deviceOSType);
    }

    @Override
    public GameOnSession createSession(final GameOnSession gameOnSession) {

        final Profile profile;

        if (gameOnSession.getProfile() == null) {
            profile = getCurrentProfileSupplier().get();
        } else {

            profile = getProfileDao().getActiveProfile(gameOnSession.getProfile().getId());

            if (!Objects.equals(getUser(), profile.getUser())) {
                final String msg = "Profile with id not found: " + gameOnSession.getProfile().getId();
                throw new ProfileNotFoundException(msg);
            }

        }

        gameOnSession.setProfile(profile);

        final GameOnSession authenticated = authenticateSession(gameOnSession);
        return getGameOnSessionDao().createSession(authenticated);

    }

    private GameOnSession authenticateSession(final GameOnSession gameOnSession) {

        final Profile profile = gameOnSession.getProfile();

        final GameOnRegistration gameOnRegistration = getGameOnRegistrationDao()
            .getRegistrationForProfile(profile);

        final GameOnApplicationConfiguration gameOnApplicationConfiguration = getGameOnApplicationConfigurationDao()
            .getDefaultConfigurationForApplication(profile.getApplication().getId());

        return getGameOnAuthenticationInvokerBuilderProvider().get()
            .withConfiguration(gameOnApplicationConfiguration)
            .withRegistration(gameOnRegistration)
            .withSession(gameOnSession)
            .build()
            .invoke();

    }

    @Override
    public void deleteSession(final String gameOnSessionId) {
        getGameOnSessionDao().deleteSessionForUser(getUser(), gameOnSessionId);
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Supplier<Profile> getCurrentProfileSupplier() {
        return currentProfileSupplier;
    }

    @Inject
    public void setCurrentProfileSupplier(Supplier<Profile> currentProfileSupplier) {
        this.currentProfileSupplier = currentProfileSupplier;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public GameOnSessionDao getGameOnSessionDao() {
        return gameOnSessionDao;
    }

    @Inject
    public void setGameOnSessionDao(GameOnSessionDao gameOnSessionDao) {
        this.gameOnSessionDao = gameOnSessionDao;
    }

    public GameOnRegistrationDao getGameOnRegistrationDao() {
        return gameOnRegistrationDao;
    }

    @Inject
    public void setGameOnRegistrationDao(GameOnRegistrationDao gameOnRegistrationDao) {
        this.gameOnRegistrationDao = gameOnRegistrationDao;
    }

    public GameOnApplicationConfigurationDao getGameOnApplicationConfigurationDao() {
        return gameOnApplicationConfigurationDao;
    }

    @Inject
    public void setGameOnApplicationConfigurationDao(GameOnApplicationConfigurationDao gameOnApplicationConfigurationDao) {
        this.gameOnApplicationConfigurationDao = gameOnApplicationConfigurationDao;
    }

    public Provider<GameOnAuthenticationInvoker.Builder> getGameOnAuthenticationInvokerBuilderProvider() {
        return gameOnAuthenticationInvokerBuilderProvider;
    }

    @Inject
    public void setGameOnAuthenticationInvokerBuilderProvider(Provider<GameOnAuthenticationInvoker.Builder> gameOnAuthenticationInvokerBuilderProvider) {
        this.gameOnAuthenticationInvokerBuilderProvider = gameOnAuthenticationInvokerBuilderProvider;
    }

}
