package com.namazustudios.socialengine.client.rest.client.gameon;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.admin.GameOnAddPrizeListRequest;
import com.namazustudios.socialengine.model.gameon.admin.GameOnAddPrizeListResponse;
import com.namazustudios.socialengine.model.gameon.admin.GameOnGetPrizeListResponse;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.*;

import static com.namazustudios.socialengine.GameOnConstants.GAMEON_ADMIN_SERVICE_ROOT;
import static com.namazustudios.socialengine.GameOnConstants.X_API_KEY;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("game_on_admin/{applicationId}/{configurationId}/prizes")
public interface GameOnPrizesClient extends RestService {

    /**
     * Fetches all {@link GameOnGetPrizeListResponse} from amazon GameOn.
     *
     * <a href="https://developer.amazon.com/docs/gameon/admin-api-ref.html#get-prize-list">GET /prizes</a>
     *
     * @param applicationId the value of {@link Application#getId()}
     * @param configurationId the value of {@link GameOnApplicationConfiguration#getId()}
     * @param callback the {@link MethodCallback} to receive the response
     */
    @GET
    @Consumes(APPLICATION_JSON)
    void getPrizes(
        @PathParam("applicationId") String applicationId,
        @PathParam("configurationId") String configurationId,
        final MethodCallback<GameOnGetPrizeListResponse> callback);

    /**
     * Creates one or more prizes from the supplied {@link GameOnAddPrizeListRequest}.
     *
     * <a href="https://developer.amazon.com/docs/gameon/admin-api-ref.html#add-prize-list">POST /prizes</a>
     *
     * @param applicationId the value of {@link Application#getId()}
     * @param configurationId the value of {@link GameOnApplicationConfiguration#getId()}
     * @param addPrizeListRequest the {@link GameOnAddPrizeListRequest} containined the necessary metadata
     * @param callback the {@link MethodCallback< GameOnAddPrizeListResponse >} used to handle the response
     */
    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    void addPrizes(
        @PathParam("applicationId") String applicationId,
        @PathParam("configurationId") String configurationId,
        GameOnAddPrizeListRequest addPrizeListRequest,
        MethodCallback<GameOnAddPrizeListResponse> callback);

}
