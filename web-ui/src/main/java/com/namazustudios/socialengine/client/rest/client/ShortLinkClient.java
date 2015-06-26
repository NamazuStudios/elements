package com.namazustudios.socialengine.client.rest.client;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ShortLink;
import com.namazustudios.socialengine.model.User;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.*;

/**
 * Client code to manipulate instances of {@link ShortLink}.
 *
 * Created by patricktwohig on 6/17/15.
 */
@Path("short_link")
public interface ShortLinkClient extends RestService {

    /**
     * Gets the offset from the dataset, with the given max count.
     *
     * @param offset the offset
     * @param count count
     * @param paginationMethodCallback
     */
    @GET
    void getShortLinks(@QueryParam("offset")int offset,
                       @QueryParam("count") int count,
                       final MethodCallback<Pagination<ShortLink>> paginationMethodCallback);

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

    /**
     * Deletes a short link given the object's id.  The id is obtained from {@link ShortLink#getId()} and
     * is assigned by the server
     *
     * @param shortLinkId the short link ID
     * @param voidMethodCallback the method callback
     */
    @DELETE
    @Path("{id}")
    void delete(@PathParam("id") final String shortLinkId,
                final MethodCallback<Void> voidMethodCallback);

}
