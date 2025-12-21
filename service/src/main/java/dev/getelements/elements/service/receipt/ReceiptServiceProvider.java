package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.receipt.ReceiptService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.service.util.Services.forbidden;

public class ReceiptServiceProvider  implements Provider<ReceiptService> {

    private User user;

    private Provider<UserReceiptService> userReceiptServiceProvider;

    private Provider<SuperuserReceiptService> superuserReceiptServiceProvider;

    @Override
    public ReceiptService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperuserReceiptServiceProvider().get();
            case USER:
                return getUserReceiptServiceProvider().get();
            default:
                return forbidden(ReceiptService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserReceiptService> getUserReceiptServiceProvider() {
        return userReceiptServiceProvider;
    }

    @Inject
    public void setUserReceiptServiceProvider(Provider<UserReceiptService> userReceiptServiceProvider) {
        this.userReceiptServiceProvider = userReceiptServiceProvider;
    }

    public Provider<SuperuserReceiptService> getSuperuserReceiptServiceProvider() {
        return superuserReceiptServiceProvider;
    }

    @Inject
    public void setSuperuserReceiptServiceProvider(Provider<SuperuserReceiptService> superuserReceiptServiceProvider) {
        this.superuserReceiptServiceProvider = superuserReceiptServiceProvider;
    }
}