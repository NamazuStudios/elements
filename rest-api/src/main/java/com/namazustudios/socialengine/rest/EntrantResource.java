package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.BasicEntrant;
import com.namazustudios.socialengine.model.SocialCampaign;
import com.namazustudios.socialengine.model.SocialCampaignEntry;
import com.namazustudios.socialengine.model.SteamEntrant;
import com.namazustudios.socialengine.service.SocialCampaignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
    @Produces(MediaType.APPLICATION_JSON)
    public SocialCampaignEntry addEntrant(@PathParam("name")final String name, final BasicEntrant basicEntrant) {
        LOGGER.info("Adding entrant for basic campaign: " + name);
        return socialCampaignService.submitEntrant(name, basicEntrant);
    }

    @POST
    @Path("steam/entrant")
    @Produces(MediaType.APPLICATION_JSON)
    public SocialCampaignEntry addEntrant(@PathParam("name")final String name, final SteamEntrant steamEntrant) {
        LOGGER.info("Adding entrant for Steam campaign: " + name);
        return socialCampaignService.submitEntrant(name, steamEntrant);
    }

}
