package com.namazustudios.socialengine.service.social;

import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.service.SocialCampaignService;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Provides the appropriate {@link com.namazustudios.socialengine.service.SocialCampaignService} based on
 * the currently active user.
 *
 * Created by patricktwohig on 3/26/15.
 */
public class SocialCampaignServiceProvider implements Provider<SocialCampaignService> {

    @Inject
    private User user;

    @Inject
    private Provider<UnprivilegedSocialCampaignService> unprivilegedSocialCampaignServiceProvider;

    @Inject
    private Provider<SuperuserSocialCampaignService> userSocialCampaignServiceProvider;

    @Override
    public SocialCampaignService get() {
        switch (user.getLevel()) {

            // Both users and superusers can create and manage campaigns, so the access
            // for both is equivalent.

            case USER:
            case SUPERUSER:
                return userSocialCampaignServiceProvider.get();

            // Realistically, only an unprivileged user can enter a competition, so this
            // service just uses that.

            default:
                return unprivilegedSocialCampaignServiceProvider.get();

        }
    }
}
