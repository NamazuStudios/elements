package com.namazustudios.promotions.rest;

import com.namazustudios.promotion.model.SteamEntrant;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Created by patricktwohig on 3/19/15.
 */
@Path("campaign/{name}/steam")
public class SteamEntrantResource {

    @POST
    public void signup(@PathParam("name")final String name, final SteamEntrant steamEntrant) {
        // TODO Sign up for Steam
    }

}
