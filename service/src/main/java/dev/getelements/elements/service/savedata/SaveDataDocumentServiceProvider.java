package dev.getelements.elements.service.savedata;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.savedata.SaveDataDocumentService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.service.util.Services.forbidden;

public class SaveDataDocumentServiceProvider implements Provider<SaveDataDocumentService> {

    private User user;

    private Provider<UserSaveDataDocumentService> userSaveDataDocumentServiceProvider;

    private Provider<SuperUserSaveDataDocumentService> superUserSaveDataDocumentServiceProvider;

    @Override
    public SaveDataDocumentService get() {
        switch (getUser().getLevel()) {
            case USER:
                return getUserSaveDataDocumentServiceProvider().get();
            case SUPERUSER:
                return getSuperUserSaveDataDocumentServiceProvider().get();
            default:
                return forbidden(SaveDataDocumentService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserSaveDataDocumentService> getUserSaveDataDocumentServiceProvider() {
        return userSaveDataDocumentServiceProvider;
    }

    @Inject
    public void setUserSaveDataDocumentServiceProvider(Provider<UserSaveDataDocumentService> userSaveDataDocumentServiceProvider) {
        this.userSaveDataDocumentServiceProvider = userSaveDataDocumentServiceProvider;
    }

    public Provider<SuperUserSaveDataDocumentService> getSuperUserSaveDataDocumentServiceProvider() {
        return superUserSaveDataDocumentServiceProvider;
    }

    @Inject
    public void setSuperUserSaveDataDocumentServiceProvider(Provider<SuperUserSaveDataDocumentService> superUserSaveDataDocumentServiceProvider) {
        this.superUserSaveDataDocumentServiceProvider = superUserSaveDataDocumentServiceProvider;
    }

}
