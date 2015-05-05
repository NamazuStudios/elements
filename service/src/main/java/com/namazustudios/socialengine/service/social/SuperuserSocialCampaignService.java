package com.namazustudios.socialengine.service.social;

import com.namazustudios.socialengine.dao.SocialCampaignDao;
import com.namazustudios.socialengine.model.BasicEntrant;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.SocialCampaign;
import com.namazustudios.socialengine.model.SocialCampaignEntry;
import com.namazustudios.socialengine.model.SteamEntrant;
import com.namazustudios.socialengine.service.SocialCampaignService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 3/26/15.
 */
public class SuperuserSocialCampaignService implements SocialCampaignService {

    @Inject
    private SocialCampaignDao socialCampaignDao;

    @Override
    public SocialCampaign createNewCampaign(SocialCampaign socialCampaign) {
        return socialCampaignDao.createNewCampaign(socialCampaign);
    }

    @Override
    public SocialCampaign updateSocialCampaign(SocialCampaign socialCampaign) {
        return socialCampaignDao.updateSocialCampaign(socialCampaign);
    }

    @Override
    public Pagination<SocialCampaign> getSocialCampaigns(int offset, int count) {
        return socialCampaignDao.getSocialCampaigns(offset, count);
    }

    @Override
    public SocialCampaign getSocialCampaign(String name) {
        return socialCampaignDao.getSocialCampaign(name);
    }

    @Override
    public SocialCampaignEntry submitEntrant(String campaign, BasicEntrant entrant) {
        return socialCampaignDao.submitEntrant(campaign, entrant);
    }

    @Override
    public SocialCampaignEntry submitEntrant(String campaign, SteamEntrant entrant) {
        return socialCampaignDao.submitEntrant(campaign, entrant);
    }

}
