package com.namazustudios.socialengine.service.gameon;

import com.namazustudios.socialengine.dao.GameOnApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.GameOnRegistrationDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.GameOnRegistration;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.GameOnRegistrationService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Objects;
import java.util.function.Supplier;

public class UserGameOnRegistrationService implements GameOnRegistrationService {

    private Supplier<Profile> currentProfileSupplier;

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
        final Profile profile = getCurrentProfileSupplier().get();
        return getGameOnRegistrationDao().getRegistrationForProfile(profile, gameOnRegistrationId);
    }

    @Override
    public Pagination<GameOnRegistration> getGameOnRegistrations(final int offset, final int count) {
        final User user = getCurrentProfileSupplier().get().getUser();
        return getGameOnRegistrationDao().getRegistrationsForUser(user, offset, count);
    }

    @Override
    public Pagination<GameOnRegistration> getGameOnRegistrations(final int offset, final int count, final String search) {
        final User user = getCurrentProfileSupplier().get().getUser();
        return getGameOnRegistrationDao().getRegistrationsForUser(user, offset, count, search);
    }

    @Override
    public GameOnRegistration createRegistration(final GameOnRegistration gameOnRegistration) {

        final Profile profile = getCurrentProfileSupplier().get();

        if (gameOnRegistration.getProfile() == null) {
            gameOnRegistration.setProfile(profile);
        } else if (!Objects.equals(gameOnRegistration.getProfile().getId(), profile.getId())) {
            throw new ForbiddenException("Profile mismatch when making registration.");
        }

        final GameOnRegistration registered = registerWithGameOn(profile, gameOnRegistration);
        return getGameOnRegistrationDao().createRegistration(registered);

    }

    private GameOnRegistration registerWithGameOn(
            final Profile profile,
            final GameOnRegistration gameOnRegistration) {

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
        final Profile profile = getCurrentProfileSupplier().get();
        getGameOnRegistrationDao().deleteRegistration(profile, gameOnRegistrationId);
    }

    public Supplier<Profile> getCurrentProfileSupplier() {
        return currentProfileSupplier;
    }

    @Inject
    public void setCurrentProfileSupplier(Supplier<Profile> currentProfileSupplier) {
        this.currentProfileSupplier = currentProfileSupplier;
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

