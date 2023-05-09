package dev.getelements.elements.service.shortlink;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.ShortLinkService;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 6/10/15.
 */
public class ShortLinkServiceProvider implements Provider<ShortLinkService> {

    @Inject
    private User user;

    @Inject
    private Provider<SuperuserShortLinkService> superuserShortLinkServiceProvider;

    @Inject
    private Provider<UnprivilegedShortLinkService> unprivilegedShortLinkServiceProvider;

    @Override
    public ShortLinkService get() {

        switch (user.getLevel()) {
            case SUPERUSER: return superuserShortLinkServiceProvider.get();
            default:        return unprivilegedShortLinkServiceProvider.get();
        }

    }

}
