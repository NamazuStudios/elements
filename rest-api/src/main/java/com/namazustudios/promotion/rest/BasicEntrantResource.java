package com.namazustudios.promotion.rest;

import com.namazustudios.promotion.model.BasicEntrant;
import com.namazustudios.promotion.model.SteamEntrant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Created by patricktwohig on 3/19/15.
 */
@Path("campaign/{name}")
public class BasicEntrantResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicEntrantResource.class);

    @POST
    @Path("basic/entrant")
    public void addEntrant(@PathParam("name")final String name, final BasicEntrant basicEntrant) {
        // TODO Sign up for Steam
        LOGGER.info("Adding entrant for basic campaign: " + name);
    }

    @POST
    @Path("steam/entrant")
    public void addEntrant(@PathParam("name")final String name, final SteamEntrant steamEntrant) {
        // TODO Sign up for Steam
        LOGGER.info("Adding entrant for Steam campaign: " + name);
    }

}
