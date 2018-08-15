package com.namazustudios.socialengine.client.rest.client.gameon;

import com.namazustudios.socialengine.model.gameon.admin.AddPrizeListRequest;
import com.namazustudios.socialengine.model.gameon.admin.AddPrizeListResponse;
import com.namazustudios.socialengine.model.gameon.admin.GetPrizeListResponse;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.Options;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.*;

import static com.namazustudios.socialengine.GameOnConstants.GAMEON_ADMIN_SERVICE_ROOT;
import static com.namazustudios.socialengine.GameOnConstants.X_API_KEY;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("prizes")
@Options(serviceRootKey = GAMEON_ADMIN_SERVICE_ROOT)
public interface GameOnPrizesClient extends RestService {

    /**
     * Fetches all {@link GetPrizeListResponse} from amazon GameOn.
     *
     * <a href="https://developer.amazon.com/docs/gameon/admin-api-ref.html#get-prize-list">GET /prizes</a>
     *
     * @param apiKey the API key
     * @param callback the {@link MethodCallback} to receive the response
     */
    @GET
    @Consumes(APPLICATION_JSON)
    void getPrizes(
        @HeaderParam(X_API_KEY) final String apiKey,
        final MethodCallback<GetPrizeListResponse> callback);

    /**
     * Creates one or more prizes from the supplied {@link AddPrizeListRequest}.
     *
     * <a href="https://developer.amazon.com/docs/gameon/admin-api-ref.html#add-prize-list">POST /prizes</a>
     *
     * @param apiKey the API key
     * @param addPrizeListRequest the {@link AddPrizeListRequest} containined the necessary metadata
     * @param callback the {@link MethodCallback<AddPrizeListResponse>} used to handle the response
     */
    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    void addPrizes(
        @HeaderParam(X_API_KEY) final String apiKey,
        final AddPrizeListRequest addPrizeListRequest,
        final MethodCallback<AddPrizeListResponse> callback);

}
