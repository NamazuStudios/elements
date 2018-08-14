package com.namazustudios.socialengine.client.rest.client.gameon;

import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import static com.namazustudios.socialengine.client.rest.client.gameon.GameOnConstants.GAMEON_SERVICE_ROOT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("prizes")
@Options(serviceRootKey = GAMEON_SERVICE_ROOT)
public interface PrizesClient extends RestService {


    @GET
    @Consumes(APPLICATION_JSON)
    void getPrizes(@HeaderParam("X-Api-Key") final String apiKey);

}
