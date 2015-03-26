package com.namazustudios.promotion.service.social;

import com.namazustudios.promotion.model.User;
import com.namazustudios.promotion.service.SocialCampaignService;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Provides the appropriate {@link com.namazustudios.promotion.service.SocialCampaignService} based on
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
