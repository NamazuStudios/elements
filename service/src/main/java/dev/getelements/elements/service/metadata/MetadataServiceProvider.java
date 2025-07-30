package dev.getelements.elements.service.metadata;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.metadata.MetadataService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class MetadataServiceProvider implements Provider<MetadataService>{

        @Inject
        private User user;

        @Inject
        private Provider<SuperUserMetadataService> superuserUserServiceProvider;

        @Inject
        private Provider<UserMetadataService> userUserServiceProvider;

        @Inject
        private Provider<AnonMetadataService> anonUserServiceProvider;

        @Override
        public MetadataService get() {

            return switch (user.getLevel()) {
                case SUPERUSER -> superuserUserServiceProvider.get();
                case USER -> userUserServiceProvider.get();
                default -> anonUserServiceProvider.get();
            };
        }

}
