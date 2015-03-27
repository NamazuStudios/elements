package com.namazustudios.promotion.service.social;

import com.namazustudios.promotion.dao.SocialCampaignDao;
import com.namazustudios.promotion.exception.NotFoundException;
import com.namazustudios.promotion.model.BasicEntrant;
import com.namazustudios.promotion.model.Pagination;
import com.namazustudios.promotion.model.SocialCampaign;
import com.namazustudios.promotion.model.SocialCampaignEntry;
import com.namazustudios.promotion.service.SocialCampaignService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 3/26/15.
 */
public class UnprivilegedSocialCampaignService implements SocialCampaignService {

    @Inject
    private SocialCampaignDao socialCampaignDao;

    @Override
    public SocialCampaign createNewCampaign(SocialCampaign socialCampaign) {
        throw new NotFoundException();
    }

    @Override
    public SocialCampaign updateSocialCampaign(SocialCampaign socialCampaign) {
        throw new NotFoundException();
    }

    @Override
    public Pagination<SocialCampaign> getSocialCampaigns(int offset, int count) {
        throw new NotFoundException();
    }

    @Override
    public SocialCampaign getSocialCampaign(String name) {
        return socialCampaignDao.getSocialCampaign(name);
    }

    @Override
    public SocialCampaignEntry submitEntrant(String campaign, BasicEntrant entrant) {
        return socialCampaignDao.submitEntrant(campaign, entrant);
    }

}

