package dev.getelements.elements.service.social;

import dev.getelements.elements.dao.SocialCampaignDao;
import dev.getelements.elements.model.BasicEntrantProfile;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.SocialCampaign;
import dev.getelements.elements.model.SocialCampaignEntry;
import dev.getelements.elements.model.SteamEntrantProfile;
import dev.getelements.elements.service.SocialCampaignService;

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
    public SocialCampaignEntry submitEntrant(String campaign, BasicEntrantProfile entrant) {
        return socialCampaignDao.submitEntrant(campaign, entrant);
    }

    @Override
    public SocialCampaignEntry submitEntrant(String campaign, SteamEntrantProfile entrant) {
        return socialCampaignDao.submitEntrant(campaign, entrant);
    }

}
