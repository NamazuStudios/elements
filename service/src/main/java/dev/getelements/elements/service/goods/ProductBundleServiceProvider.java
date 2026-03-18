package dev.getelements.elements.service.goods;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.goods.ProductBundleService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class ProductBundleServiceProvider implements Provider<ProductBundleService> {

    private User user;

    private Provider<SuperuserProductBundleService> superuserProductBundleServiceProvider;

    private Provider<UserProductBundleService> userProductBundleServiceProvider;

    @Override
    public ProductBundleService get() {
        return switch (getUser().getLevel()) {
            case SUPERUSER -> getSuperuserProductBundleServiceProvider().get();
            case USER -> getUserProductBundleServiceProvider().get();
            default -> throw new ForbiddenException("Unprivileged requests cannot access Product Bundle records.");
        };
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperuserProductBundleService> getSuperuserProductBundleServiceProvider() {
        return superuserProductBundleServiceProvider;
    }

    @Inject
    public void setSuperuserProductBundleServiceProvider(
            Provider<SuperuserProductBundleService> superuserProductBundleServiceProvider) {
        this.superuserProductBundleServiceProvider = superuserProductBundleServiceProvider;
    }

    public Provider<UserProductBundleService> getUserProductBundleServiceProvider() {
        return userProductBundleServiceProvider;
    }

    @Inject
    public void setUserProductBundleServiceProvider(
            Provider<UserProductBundleService> userProductBundleServiceProvider) {
        this.userProductBundleServiceProvider = userProductBundleServiceProvider;
    }

}
