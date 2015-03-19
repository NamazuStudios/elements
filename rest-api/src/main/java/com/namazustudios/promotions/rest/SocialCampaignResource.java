package com.namazustudios.promotions.rest;

import com.namazustudios.promotion.model.SocialCampaign;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Collections;
import java.util.List;

/**
 * Created by patricktwohig on 3/18/15.
 */
@Path("campaign")
public class SocialCampaignResource {

    @GET
    public List<SocialCampaign> getSocialCampaigns() {
        // TODO
        return Collections.emptyList();
    }

    @GET
    @Path("{name}")
    public SocialCampaign getSocialCampaign(@PathParam("name") final String name) {
        // TODO
        return null;
    }

    @POST
    public void createSocialCampaign(final SocialCampaign socialCampaign) {
        // TODO
    }

}
