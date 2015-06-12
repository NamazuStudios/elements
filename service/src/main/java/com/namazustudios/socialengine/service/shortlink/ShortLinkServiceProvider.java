package com.namazustudios.socialengine.service.shortlink;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.Services;
import com.namazustudios.socialengine.service.ShortLinkService;
import com.namazustudios.socialengine.service.auth.UserAuthService;

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
