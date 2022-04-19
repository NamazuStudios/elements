package com.namazustudios.socialengine.service.savedata;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.SaveDataDocumentService;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

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
