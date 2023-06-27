package dev.getelements.elements.rest;

import dev.getelements.elements.util.ValidationHelper;
import dev.getelements.elements.exception.InvalidParameterException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.SocialCampaign;
import dev.getelements.elements.service.SocialCampaignService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by patricktwohig on 3/18/15.
 */
@Path("campaign")
public class SocialCampaignResource {

    @Inject
    private SocialCampaignService socialCampaignService;

    @Inject
    private ValidationHelper validationService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Pagination<SocialCampaign> getSocialCampaigns(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("search") final String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        return socialCampaignService.getSocialCampaigns(offset, count);

    }

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public SocialCampaign getSocialCampaign(@PathParam("name") final String name) {
        return socialCampaignService.getSocialCampaign(name);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public SocialCampaign createSocialCampaign(final SocialCampaign socialCampaign) {
        validationService.validateModel(socialCampaign);
        return socialCampaignService.createNewCampaign(socialCampaign);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public SocialCampaign updateSocialCampaign(final SocialCampaign socialCampaign) {
        validationService.validateModel(socialCampaign);
        return socialCampaignService.updateSocialCampaign(socialCampaign);
    }

}
