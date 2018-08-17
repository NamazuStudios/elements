package com.namazustudios.socialengine.service.gameon.client.invoker.v1;

import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.model.gameon.admin.GameOnAddPrizeListRequest;
import com.namazustudios.socialengine.model.gameon.admin.GameOnAddPrizeListResponse;
import com.namazustudios.socialengine.model.gameon.admin.GameOnGetPrizeListResponse;
import com.namazustudios.socialengine.service.gameon.client.invoker.GameOnPrizeInvoker;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static com.namazustudios.socialengine.GameOnConstants.ADMIN_BASE_API;
import static com.namazustudios.socialengine.GameOnConstants.VERSION_V1;
import static com.namazustudios.socialengine.GameOnConstants.X_API_KEY;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class V1StandardGameOnPrizeInvoker implements GameOnPrizeInvoker {

    public static final String PRIZES_PATH = "prizes";

    private final Client client;

    private final String adminApiKey;

    public V1StandardGameOnPrizeInvoker(final Client client, final String adminApiKey) {
        this.client = client;
        this.adminApiKey = adminApiKey;
    }

    @Override
    public GameOnGetPrizeListResponse getPrizes() {

        final Response response = client
            .target(ADMIN_BASE_API)
            .path(VERSION_V1).path(PRIZES_PATH)
            .request()
            .header(X_API_KEY, adminApiKey)
            .get();

        return getResponse(response, GameOnGetPrizeListResponse.class);

    }

    @Override
    public GameOnAddPrizeListResponse addPrizes(final GameOnAddPrizeListRequest gameOnAddPrizeListRequest) {

        final Response response = client
            .target(ADMIN_BASE_API)
            .path(VERSION_V1).path(PRIZES_PATH)
            .request()
            .header(X_API_KEY, adminApiKey)
            .post(entity(gameOnAddPrizeListRequest, APPLICATION_JSON));

        return getResponse(response, GameOnAddPrizeListResponse.class);

    }

    private <T> T getResponse(final Response response, Class<T> responseTClass) {
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            return response.readEntity(responseTClass);
        } else if (response.getStatus() != Response.Status.FORBIDDEN.getStatusCode()) {
            throw new ForbiddenException("GameOn API Rejected Request");
        } else if (response.getStatus() != Response.Status.BAD_REQUEST.getStatusCode()) {
            throw new InvalidDataException("GameOn API returned Bad Request.  Check Configuration.");
        } else {
            throw new InternalException("Error Interacting with GameOn API.");
        }
    }

}
