package dev.getelements.elements.service.codegen;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.codegen.CodegenService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class OpenApiCodegenServiceProvider implements Provider<CodegenService> {

    private User user;

    private Provider<SuperUserOpenApiCodegenService> superUserOpenApiCodegenServiceProvider;

    @Override
    public CodegenService get() {

        switch (user.getLevel()) {
            case SUPERUSER:
                return superUserOpenApiCodegenServiceProvider.get();
            default:
                throw new ForbiddenException("You are not authorized to perform this operation");
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserOpenApiCodegenService> getSuperUserOpenApiCodegenServiceProvider() {
        return superUserOpenApiCodegenServiceProvider;
    }

    @Inject
    public void setSuperUserOpenApiCodegenServiceProvider(Provider<SuperUserOpenApiCodegenService> superUserOpenApiCodegenServiceProvider) {
        this.superUserOpenApiCodegenServiceProvider = superUserOpenApiCodegenServiceProvider;
    }
}
