package com.namazustudios.socialengine.server;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.model.UiConfig;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static com.namazustudios.socialengine.GameOnConstants.ADMIN_BASE_API;
import static com.namazustudios.socialengine.GameOnConstants.VERSION_V1;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * A simple RESTful endpoint which is used to deliver ui configuration to the client code.  This is not included
 * in the core RESTful API as it is only used to support the UI code.  However, it may supply.
 *
 * Created by patricktwohig on 5/9/17.
 */
@Path("config")
public class UiConfigResource {

    private String outsideApiUrl;

    @GET
    @Produces(APPLICATION_JSON)
    public UiConfig getUiConfig() {
        final UiConfig uiConfig = new UiConfig();
        uiConfig.setApiUrl(getOutsideApiUrl());
        return uiConfig;
    }

    public String getOutsideApiUrl() {
        return outsideApiUrl;
    }

    @Inject
    public void setOutsideApiUrl(@Named(Constants.API_OUTSIDE_URL) String outsideApiUrl) {
        this.outsideApiUrl = outsideApiUrl;
    }

}
