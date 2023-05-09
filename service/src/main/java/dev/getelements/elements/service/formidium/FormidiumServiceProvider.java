package dev.getelements.elements.service.formidium;

import dev.getelements.elements.model.user.User;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static dev.getelements.elements.service.Services.forbidden;
import static dev.getelements.elements.service.Services.unimplemented;
import static dev.getelements.elements.service.formidium.FormidiumConstants.FORMIDIUM_API_KEY;

public class FormidiumServiceProvider implements Provider<FormidiumService> {

    private User user;

    private String formidiumApiKey;

    private Provider<UserFormidiumService> userFormidiumServiceProvider;

    private Provider<SuperuserFormidiumService> superuserFormidiumServiceProvider;

    @Override
    public FormidiumService get() {

        if (formidiumApiKey.isBlank()) {
            return unimplemented(FormidiumService.class);
        }

        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperuserFormidiumServiceProvider().get();
            case USER:
                return getUserFormidiumServiceProvider().get();
            default:
                return forbidden(FormidiumService.class);
        }

    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<UserFormidiumService> getUserFormidiumServiceProvider() {
        return userFormidiumServiceProvider;
    }

    public String getFormidiumApiKey() {
        return formidiumApiKey;
    }

    @Inject
    public void setFormidiumApiKey(@Named(FORMIDIUM_API_KEY) String formidiumApiKey) {
        this.formidiumApiKey = formidiumApiKey;
    }

    @Inject
    public void setUserFormidiumServiceProvider(Provider<UserFormidiumService> userFormidiumServiceProvider) {
        this.userFormidiumServiceProvider = userFormidiumServiceProvider;
    }

    public Provider<SuperuserFormidiumService> getSuperuserFormidiumServiceProvider() {
        return superuserFormidiumServiceProvider;
    }

    @Inject
    public void setSuperuserFormidiumServiceProvider(Provider<SuperuserFormidiumService> superuserFormidiumServiceProvider) {
        this.superuserFormidiumServiceProvider = superuserFormidiumServiceProvider;
    }

}
