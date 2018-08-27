package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.dao.GameOnApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.GameOnRegistrationDao;
import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.exception.ProfileNotFoundException;
import com.namazustudios.socialengine.exception.gameon.GameOnRegistrationNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.game.GameOnRegistration;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.GameOnRegistrationService;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnRegistrationInvoker;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Objects;
import java.util.function.Supplier;

public class UserGameOnRegistrationService implements GameOnRegistrationService {

    private User user;

    private Supplier<Profile> currentProfileSupplier;

    private ProfileDao profileDao;

    private GameOnRegistrationDao gameOnRegistrationDao;

    private GameOnApplicationConfigurationDao gameOnApplicationConfigurationDao;

    private Provider<GameOnRegistrationInvoker.Builder> gameOnRegistrationInvokerBuilderProvider;

    @Override
    public GameOnRegistration getCurrentGameOnRegistration() {
        final Profile profile = getCurrentProfileSupplier().get();
        return getGameOnRegistrationDao().getRegistrationForProfile(profile);
    }

    @Override
    public GameOnRegistration getGameOnRegistration(final String gameOnRegistrationId) {
        return getGameOnRegistrationDao().getRegistrationForUser(getUser(), gameOnRegistrationId);
    }

    @Override
    public Pagination<GameOnRegistration> getGameOnRegistrations(final int offset, final int count) {
        return getGameOnRegistrationDao().getRegistrationsForUser(getUser(), offset, count);
    }

    @Override
    public Pagination<GameOnRegistration> getGameOnRegistrations(final int offset, final int count, final String search) {
        return getGameOnRegistrationDao().getRegistrationsForUser(getUser(), offset, count, search);
    }

    @Override
    public GameOnRegistration createRegistration(final GameOnRegistration gameOnRegistration) {

        final Profile profile;

        if (gameOnRegistration.getProfile() == null) {
            profile = getCurrentProfileSupplier().get();
        } else {

            profile = getProfileDao().getActiveProfile(gameOnRegistration.getProfile().getId());

            if (!Objects.equals(getUser(), profile.getUser())) {
                final String msg = "Profile with id not found: " + gameOnRegistration.getProfile().getId();
                throw new ProfileNotFoundException(msg);
            }

        }

        gameOnRegistration.setProfile(profile);

        final GameOnRegistration registered = registerWithGameOn(gameOnRegistration);
        return getGameOnRegistrationDao().createRegistration(registered);

    }

    @Override
    public GameOnRegistration createOrGetCurrentRegistration() {

        final Profile profile = getCurrentProfileSupplier().get();

        try {
            return getGameOnRegistrationDao().getRegistrationForProfile(profile);
        } catch (GameOnRegistrationNotFoundException ex) {
            final GameOnRegistration gameOnRegistration = new GameOnRegistration();
            gameOnRegistration.setProfile(profile);

            final GameOnRegistration registered = registerWithGameOn(gameOnRegistration);
            return getGameOnRegistrationDao().createRegistration(registered);
        }

    }

    private GameOnRegistration registerWithGameOn(final GameOnRegistration gameOnRegistration) {

        final Profile profile = gameOnRegistration.getProfile();

        final GameOnApplicationConfiguration gameOnApplicationConfiguration = getGameOnApplicationConfigurationDao()
            .getDefaultConfigurationForApplication(profile.getApplication().getId());

        return getGameOnRegistrationInvokerBuilderProvider().get()
            .withRegistration(gameOnRegistration)
            .withConfiguration(gameOnApplicationConfiguration)
            .build()
            .invoke();

    }

    @Override
    public void deleteRegistration(final String gameOnRegistrationId) {
        getGameOnRegistrationDao().deleteRegistrationForUser(getUser(), gameOnRegistrationId);
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

    public GameOnRegistrationDao getGameOnRegistrationDao() {
        return gameOnRegistrationDao;
    }

    @Inject
    public void setGameOnRegistrationDao(final GameOnRegistrationDao gameOnRegistrationDao) {
        this.gameOnRegistrationDao = gameOnRegistrationDao;
    }

    public GameOnApplicationConfigurationDao getGameOnApplicationConfigurationDao() {
        return gameOnApplicationConfigurationDao;
    }

    @Inject
    public void setGameOnApplicationConfigurationDao(GameOnApplicationConfigurationDao gameOnApplicationConfigurationDao) {
        this.gameOnApplicationConfigurationDao = gameOnApplicationConfigurationDao;
    }

    public Provider<GameOnRegistrationInvoker.Builder> getGameOnRegistrationInvokerBuilderProvider() {
        return gameOnRegistrationInvokerBuilderProvider;
    }

    @Inject
    public void setGameOnRegistrationInvokerBuilderProvider(Provider<GameOnRegistrationInvoker.Builder> gameOnRegistrationInvokerBuilderProvider) {
        this.gameOnRegistrationInvokerBuilderProvider = gameOnRegistrationInvokerBuilderProvider;
    }

}

