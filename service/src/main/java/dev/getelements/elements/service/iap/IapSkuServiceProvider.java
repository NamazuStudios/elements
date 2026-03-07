package dev.getelements.elements.service.iap;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.iap.IapSkuService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class IapSkuServiceProvider implements Provider<IapSkuService> {

    private User user;

    private Provider<SuperuserIapSkuService> superuserIapSkuServiceProvider;

    private Provider<UserIapSkuService> userIapSkuServiceProvider;

    @Override
    public IapSkuService get() {
        return switch (getUser().getLevel()) {
            case SUPERUSER -> getSuperuserIapSkuServiceProvider().get();
            case USER -> getUserIapSkuServiceProvider().get();
            default -> throw new ForbiddenException("Unprivileged requests cannot access IAP SKU records.");
        };
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperuserIapSkuService> getSuperuserIapSkuServiceProvider() {
        return superuserIapSkuServiceProvider;
    }

    @Inject
    public void setSuperuserIapSkuServiceProvider(Provider<SuperuserIapSkuService> superuserIapSkuServiceProvider) {
        this.superuserIapSkuServiceProvider = superuserIapSkuServiceProvider;
    }

    public Provider<UserIapSkuService> getUserIapSkuServiceProvider() {
        return userIapSkuServiceProvider;
    }

    @Inject
    public void setUserIapSkuServiceProvider(Provider<UserIapSkuService> userIapSkuServiceProvider) {
        this.userIapSkuServiceProvider = userIapSkuServiceProvider;
    }

}
