package com.namazustudios.socialengine.rest;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ShortLink;
import com.namazustudios.socialengine.service.ShortLinkService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * Created by patricktwohig on 6/10/15.
 */
@Path("short_link")
public class ShortLinkResource {

    @Inject
    private ShortLinkService shortLinkService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Pagination<ShortLink> getShortLinks(
            @QueryParam("offset") @DefaultValue("0")  int offset,
            @QueryParam("count")  @DefaultValue("20") int count,
            @QueryParam("search") @DefaultValue("")   String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        return Strings.nullToEmpty(search).trim().isEmpty() ?
                shortLinkService.getShortLinks(offset, count) :
                shortLinkService.getShortLinks(offset, count, search);

    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ShortLink getShortLink(@PathParam("id") @DefaultValue("") final String id) {
        return shortLinkService.getShortLink(id);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ShortLink create(final ShortLink shortLink) {
        return shortLinkService.create(shortLink);
    }

    @DELETE
    @Path("{id}")
    public void delete(@PathParam("id") @DefaultValue("") final String id) {
        shortLinkService.deleteShortLink(id);
    }

    /**
     * Gets the redirection for a link.  This returns a Response and redirects
     * to the link.
     *
     * This is not intended to be use as the raw short-linking endpoint but
     * rather provides a destination for a rewrite or something similar.
     *
     * @param path the link ID
     * @return a {@link Response} with {@link javax.ws.rs.core.Response.Status#}
     */
    @GET
    @Path("redirection/{path}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRedirection(@PathParam("path") final String path) {

        final ShortLink shortLink = shortLinkService.getShortLinkWithPath(path);

        try {
            return Response
                .status(Response.Status.MOVED_PERMANENTLY)
                .location(new URI(shortLink.getDestinationURL()))
                .entity(shortLink)
            .build();
        } catch (URISyntaxException e) {
            throw new NotFoundException();
        }

    }

}
