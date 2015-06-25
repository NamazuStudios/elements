package com.namazustudios.socialengine.client.rest.client;

import com.namazustudios.socialengine.model.ShortLink;
import com.namazustudios.socialengine.model.User;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * Client code to manipulate instances of {@link ShortLink}.
 *
 * Created by patricktwohig on 6/17/15.
 */
@Path("short_link")
public interface ShortLinkClient extends RestService {

    /**
     * Creates a new ShortLink given the ShortLink object, password, and the method callback.
     *
     * @param shortLink the ShortLink object to create
     * @param shortLinkMethodCallback the callback
     *
     */
    @POST
    void createNewShortLink(
            final ShortLink shortLink,
            final MethodCallback<ShortLink> shortLinkMethodCallback);

}
