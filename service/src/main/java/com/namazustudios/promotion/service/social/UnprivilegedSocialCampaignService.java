package com.namazustudios.promotion.service.social;

import com.namazustudios.promotion.exception.ForbiddenException;
import com.namazustudios.promotion.exception.NotFoundException;
import com.namazustudios.promotion.model.BasicEntrant;
import com.namazustudios.promotion.model.PaginatedEntry;
import com.namazustudios.promotion.model.SocialCampaign;
import com.namazustudios.promotion.model.SocialCampaignEntry;
import com.namazustudios.promotion.service.SocialCampaignService;

/**
 * Created by patricktwohig on 3/26/15.
 */
public class UnprivilegedSocialCampaignService implements SocialCampaignService {

    @Override
    public SocialCampaign createNewCampaign(SocialCampaign socialCampaign) {
        throw new NotFoundException();
    }

    @Override
    public SocialCampaign updateSocialCampaign(SocialCampaign socialCampaign) {
        throw new NotFoundException();
    }

    @Override
    public PaginatedEntry<SocialCampaign> getSocialCampaigns(int offset, int count) {
        throw new NotFoundException();
    }

    @Override
    public SocialCampaign getSocialCampaign(String name) {
        return null;
    }

    @Override
    public SocialCampaignEntry submitEntrant(String campaign, BasicEntrant entrant) {
        return null;
    }

}

