package dev.getelements.elements.service.goods;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.goods.ProductSkuSchemaService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class ProductSkuSchemaServiceProvider implements Provider<ProductSkuSchemaService> {

    private User user;

    private Provider<SuperuserProductSkuSchemaService> superuserProductSkuSchemaServiceProvider;

    private Provider<UserProductSkuSchemaService> userProductSkuSchemaServiceProvider;

    @Override
    public ProductSkuSchemaService get() {
        return switch (getUser().getLevel()) {
            case SUPERUSER -> getSuperuserProductSkuSchemaServiceProvider().get();
            case USER -> getUserProductSkuSchemaServiceProvider().get();
            default -> throw new ForbiddenException("Unprivileged requests cannot access Product SKU Schema records.");
        };
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperuserProductSkuSchemaService> getSuperuserProductSkuSchemaServiceProvider() {
        return superuserProductSkuSchemaServiceProvider;
    }

    @Inject
    public void setSuperuserProductSkuSchemaServiceProvider(Provider<SuperuserProductSkuSchemaService> superuserProductSkuSchemaServiceProvider) {
        this.superuserProductSkuSchemaServiceProvider = superuserProductSkuSchemaServiceProvider;
    }

    public Provider<UserProductSkuSchemaService> getUserProductSkuSchemaServiceProvider() {
        return userProductSkuSchemaServiceProvider;
    }

    @Inject
    public void setUserProductSkuSchemaServiceProvider(Provider<UserProductSkuSchemaService> userProductSkuSchemaServiceProvider) {
        this.userProductSkuSchemaServiceProvider = userProductSkuSchemaServiceProvider;
    }

}
