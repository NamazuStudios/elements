package dev.getelements.elements.service.goods;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.goods.ProductSkuService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class ProductSkuServiceProvider implements Provider<ProductSkuService> {

    private User user;

    private Provider<SuperuserProductSkuService> superuserProductSkuServiceProvider;

    private Provider<UserProductSkuService> userProductSkuServiceProvider;

    @Override
    public ProductSkuService get() {
        return switch (getUser().getLevel()) {
            case SUPERUSER -> getSuperuserProductSkuServiceProvider().get();
            case USER -> getUserProductSkuServiceProvider().get();
            default -> throw new ForbiddenException("Unprivileged requests cannot access Product SKU records.");
        };
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperuserProductSkuService> getSuperuserProductSkuServiceProvider() {
        return superuserProductSkuServiceProvider;
    }

    @Inject
    public void setSuperuserProductSkuServiceProvider(Provider<SuperuserProductSkuService> superuserProductSkuServiceProvider) {
        this.superuserProductSkuServiceProvider = superuserProductSkuServiceProvider;
    }

    public Provider<UserProductSkuService> getUserProductSkuServiceProvider() {
        return userProductSkuServiceProvider;
    }

    @Inject
    public void setUserProductSkuServiceProvider(Provider<UserProductSkuService> userProductSkuServiceProvider) {
        this.userProductSkuServiceProvider = userProductSkuServiceProvider;
    }

}
