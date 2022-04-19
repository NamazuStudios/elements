package com.namazustudios.socialengine.rest.blockchain;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.BlockchainConstants.MintStatus;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.template.CreateTokenTemplateRequest;
import com.namazustudios.socialengine.model.blockchain.template.TokenTemplate;
import com.namazustudios.socialengine.model.blockchain.template.UpdateTokenTemplateRequest;
import com.namazustudios.socialengine.service.blockchain.TokenTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;

/**
 * Created by tuantran on 04/12/22.
 */
@Api(value = "Token Templates",
        description = "Allows for the storage and retrieval of compiled token templates.",
        authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("/blockchain/token/template")
public class TokenTemplateResource {

    private TokenTemplateService tokenTemplateService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get Token Templates",
            notes = "Gets a pagination of Token Templates for the given query.")
    public Pagination<TokenTemplate> getTokens(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count) {
        return getTokenService().getTokens(offset, count);
    }

    @GET
    @Path("{tokenNameOrId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a specific Token Template",
            notes = "Gets a specific TokenTemplate by token name or Id.")
    public TokenTemplate getToken(@PathParam("tokenNameOrId") String tokenNameOrId) {
        return getTokenService().getTokenTemplate(tokenNameOrId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new Token Template definition",
            notes = "Creates a new Token Template definition.")
    public TokenTemplate createToken(final CreateTokenTemplateRequest tokenRequest) {
        return getTokenService().createTokenTemplate(tokenRequest);
    }

    @PUT
    @Path("{templateId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a Token Template",
            notes = "Updates a TokenTemplate with the specified id.")
    public TokenTemplate updateToken(@PathParam("templateId") String templateId, final UpdateTokenTemplateRequest tokenRequest) {
        return getTokenService().updateTokenTemplate(templateId, tokenRequest);
    }

    @DELETE
    @Path("{templateId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a TokenTemplate",
            notes = "Deletes a TokenTemplate with the specified id.")
    public void deleteToken(@PathParam("templateId") String templateId) {

        templateId = Strings.nullToEmpty(templateId).trim();

        if (templateId.isEmpty()) {
            throw new NotFoundException();
        }

        getTokenService().deleteTokenTemplate(templateId);
    }

    public TokenTemplateService getTokenService() {
        return tokenTemplateService;
    }

    @Inject
    public void setTokenService(TokenTemplateService tokenTemplateService) {
        this.tokenTemplateService = tokenTemplateService;
    }

}
