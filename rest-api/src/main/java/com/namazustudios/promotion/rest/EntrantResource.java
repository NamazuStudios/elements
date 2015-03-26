package com.namazustudios.promotion.rest;

import com.namazustudios.promotion.model.BasicEntrant;
import com.namazustudios.promotion.model.SocialCampaign;
import com.namazustudios.promotion.model.SocialCampaignEntry;
import com.namazustudios.promotion.model.SteamEntrant;
import com.namazustudios.promotion.service.SocialCampaignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Created by patricktwohig on 3/19/15.
 */
@Path("campaign/{name}")
public class EntrantResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntrantResource.class);

    @Inject
    private SocialCampaignService socialCampaignService;

    @POST
    @Path("basic/entrant")
    public SocialCampaignEntry addEntrant(@PathParam("name")final String name, final BasicEntrant basicEntrant) {
        LOGGER.info("Adding entrant for basic campaign: " + name);
        return socialCampaignService.submitEntrant(name, basicEntrant);
    }

    @POST
    @Path("steam/entrant")
    public SocialCampaignEntry addEntrant(@PathParam("name")final String name, final SteamEntrant steamEntrant) {
        LOGGER.info("Adding entrant for Steam campaign: " + name);
        return socialCampaignService.submitEntrant(name, steamEntrant);
    }

}
