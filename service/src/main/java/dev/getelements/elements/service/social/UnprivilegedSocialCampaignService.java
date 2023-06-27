package dev.getelements.elements.service.social;

import dev.getelements.elements.dao.SocialCampaignDao;
import dev.getelements.elements.exception.NotFoundException;
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
    public SocialCampaignEntry submitEntrant(String campaign, BasicEntrantProfile entrant) {
        return socialCampaignDao.submitEntrant(campaign, entrant);
    }

    @Override
    public SocialCampaignEntry submitEntrant(String campaign, SteamEntrantProfile entrant) {
        return socialCampaignDao.submitEntrant(campaign, entrant);
    }

}

