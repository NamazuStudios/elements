package dev.getelements.elements.rest.schema;

import com.google.common.base.Strings;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.schema.template.CreateTokenTemplateRequest;
import dev.getelements.elements.model.schema.template.TokenTemplate;
import dev.getelements.elements.model.schema.template.UpdateTokenTemplateRequest;
import dev.getelements.elements.service.schema.TokenTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static dev.getelements.elements.rest.swagger.EnhancedApiListingResource.*;

/**
 * Created by tuantran on 04/12/22.
 */
@Api(value = "Token Template",
        description = "Allows for the storage and retrieval of Token Template.",
        authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("/schema/token_template")
public class TokenTemplateResource {

    private TokenTemplateService tokenTemplateService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get Token Template",
            notes = "Gets a pagination of Token Template for the given query.")
    public Pagination<TokenTemplate> getTokenTemplates(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count) {
        return getTokenTemplateService().getTokenTemplates(offset, count);
    }

    @GET
    @Path("{tokenTemplateNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a specific Token Template",
            notes = "Gets a specific TokenTemplate by name or Id.")
    public TokenTemplate getTokenTemplate(@PathParam("tokenTemplateNameOrId") String tokenTemplateNameOrId) {
        return getTokenTemplateService().getTokenTemplate(tokenTemplateNameOrId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new Token Template definition",
            notes = "Creates a new Token Template definition.")
    public TokenTemplate createTokenTemplate(final CreateTokenTemplateRequest tokenRequest) {
        return getTokenTemplateService().createTokenTemplate(tokenRequest);
    }

    @PUT
    @Path("{tokenTemplateId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a Token Template",
            notes = "Updates a TokenTemplate with the specified id.")
    public TokenTemplate updateTokenTemplate(@PathParam("tokenTemplateId") String tokenTemplateId, final UpdateTokenTemplateRequest updateTokenTemplateRequest) {
        return getTokenTemplateService().updateTokenTemplate(tokenTemplateId, updateTokenTemplateRequest);
    }

    @DELETE
    @Path("{tokenTemplateId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a TokenTemplate",
            notes = "Deletes a TokenTemplate with the specified id.")
    public void deleteTokenTemplate(@PathParam("tokenTemplateId") String tokenTemplateId) {

        tokenTemplateId = Strings.nullToEmpty(tokenTemplateId).trim();

        if (tokenTemplateId.isEmpty()) {
            throw new NotFoundException();
        }

        getTokenTemplateService().deleteTokenTemplate(tokenTemplateId);
    }

    public TokenTemplateService getTokenTemplateService() {
        return tokenTemplateService;
    }

    @Inject
    public void setTokenTemplateService(TokenTemplateService tokenTemplateService) {
        this.tokenTemplateService = tokenTemplateService;
    }

}
