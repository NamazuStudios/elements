package com.namazustudios.socialengine.client.rest.client.internal;

import com.namazustudios.socialengine.model.UiConfig;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created by patricktwohig on 5/11/17.
 */
@Path("config")
public interface UiConfigClient extends RestService {

    @GET
    void getUiConfig(MethodCallback<UiConfig> uiConfigMethodCallback);

}
