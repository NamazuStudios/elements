package com.namazustudios.promotion.rest;

import com.namazustudios.promotion.model.BasicEntrant;
import com.namazustudios.promotion.model.SteamEntrant;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Created by patricktwohig on 3/19/15.
 */
@Path("campaign/{name}")
public class BasicEntrantResource {

    @POST
    @Path("basic/entrant")
    public void addEntrant(@PathParam("name")final String name, final BasicEntrant basicEntrant) {
        // TODO Sign up for Steam
    }

    @POST
    @Path("steam/entrant")
    public void addEntrant(@PathParam("name")final String name, final SteamEntrant steamEntrant) {
        // TODO Sign up for Steam
    }

}
