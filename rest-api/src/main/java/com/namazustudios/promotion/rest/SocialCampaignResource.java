package com.namazustudios.promotion.rest;

import com.namazustudios.promotion.model.PaginatedEntry;
import com.namazustudios.promotion.model.SocialCampaign;
import com.namazustudios.promotion.service.SocialCampaignService;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Collections;
import java.util.List;

/**
 * Created by patricktwohig on 3/18/15.
 */
@Path("campaign")
public class SocialCampaignResource {

    @Inject
    private SocialCampaignService socialCampaignService;

    @GET
    public PaginatedEntry<SocialCampaign> getSocialCampaigns(
            @PathParam("offset") @DefaultValue("0") int offset,
            @PathParam("count") @DefaultValue("20") int count) {
        return socialCampaignService.getSocialCampaigns(offset, count);
    }

    @GET
    @Path("{name}")
    public SocialCampaign getSocialCampaign(@PathParam("name") final String name) {
        return socialCampaignService.getSocialCampaign(name);
    }

    @POST
    public SocialCampaign createSocialCampaign(final SocialCampaign socialCampaign) {
        return socialCampaignService.createNewCampaign(socialCampaign);
    }

    @PUT
    public SocialCampaign updateSocialCampaign(final SocialCampaign socialCampaign) {
        return socialCampaignService.updateSocialCampaign(socialCampaign);
    }

}
