package com.namazustudios.promotion.service;

import com.namazustudios.promotion.model.BasicEntrant;
import com.namazustudios.promotion.model.Pagination;
import com.namazustudios.promotion.model.SocialCampaign;
import com.namazustudios.promotion.model.SocialCampaignEntry;
import com.namazustudios.promotion.model.SteamEntrant;

/**
 * Created by patricktwohig on 3/18/15.
 */
public interface SocialCampaignService {

    /**
     * Creates a new SocialCampaign.
     *
     * @param socialCampaign the socialCampaign
     *
     * @return the new campaign
     */
    public SocialCampaign createNewCampaign(final SocialCampaign socialCampaign);

    /**
     * Updates the given SocialCampaign object.
     *
     * @param socialCampaign
     * @return the updated campaign
     */
    public SocialCampaign updateSocialCampaign(final SocialCampaign socialCampaign);

    /**
     * Gets all available social campaigns.
     *
     * @return all available social campaigns
     */
    public Pagination<SocialCampaign> getSocialCampaigns(int offset, int count);

    /**
     * Gets the social campaign.
     *
     * @param name the name of the campaign
     *
     * @return the SocialCampaign with the given name.
     */
    public SocialCampaign getSocialCampaign(final String name);

    /**
     * Given the campaign name and the entrant's information, this places the entrant into
     * the campaign.
     *
     * The Entrant may be created, or may be linked from a previous campaign.
     *
     * @param entrant
     * @return the SocialCampaignEntry object, which indicates which URL the user should share
     */
    public SocialCampaignEntry submitEntrant(final String campaign, final BasicEntrant entrant);

    /**
     * Given the campaign name and the entrant's information, this places the entrant into
     * the campaign.
     *
     * The Entrant may be created, or may be linked from a previous campaign.
     *
     * @param entrant
     * @return the SocialCampaignEntry object, which indicates which URL the user should share
     */
    public SocialCampaignEntry submitEntrant(final String campaign, final SteamEntrant entrant);

}
