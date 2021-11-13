package com.namazustudios.socialengine.rest.blockchain;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.blockchain.CreateSmartContractTemplateRequest;
import com.namazustudios.socialengine.model.blockchain.SmartContractTemplate;
import com.namazustudios.socialengine.model.blockchain.UpdateSmartContractTemplateRequest;
import com.namazustudios.socialengine.service.blockchain.SmartContractTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;

/**
 * Created by keithhudnall on 9/21/21.
 */
@Api(value = "Neo Smart Contract Templates",
        description = "Allows for the storage and retrieval of compiled Neo smart contracts.",
        authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("blockchain/neo/template")
public class NeoSmartContractTemplateResource {

    private SmartContractTemplateService smartContractTemplateService;

    @GET
    @Path("{templateId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Gets a specific Neo Smart Contract Template",
            notes = "Gets a specific Neo Smart Contract Template by templateId.")
    public SmartContractTemplate getTemplate(@PathParam("templateId") String templateId) {

        templateId = Strings.nullToEmpty(templateId).trim();

        if (templateId.isEmpty()) {
            throw new NotFoundException();
        }

        return getSmartContractTemplateService().getSmartContractTemplate(templateId);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a new Neo Smart Contract Template",
            notes = "Creates a new Neo Smart Contract Template, associated with the specified application.")
    public SmartContractTemplate createTemplate(final CreateSmartContractTemplateRequest request) {
        return getSmartContractTemplateService().createSmartContractTemplate(request);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates a Neo Smart Contract Template",
            notes = "Updates a Neo Smart Contract Template with the specified name or id.")
    public SmartContractTemplate updateTemplate(final UpdateSmartContractTemplateRequest request) {
        return getSmartContractTemplateService().updateSmartContractTemplate(request);
    }

    @DELETE
    @Path("{templateId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Deletes a Neo Smart Contract Template",
            notes = "Deletes a Neo Smart Contract Template with the specified name or id.")
    public void deleteTemplate(@PathParam("templateId") String nameOrId) {

        nameOrId = Strings.nullToEmpty(nameOrId).trim();

        if (nameOrId.isEmpty()) {
            throw new NotFoundException();
        }

        getSmartContractTemplateService().deleteTemplate(nameOrId);
    }

    public SmartContractTemplateService getSmartContractTemplateService() {
        return smartContractTemplateService;
    }

    @Inject
    public void setSmartContractTemplateService(SmartContractTemplateService smartContractTemplateService) {
        this.smartContractTemplateService = smartContractTemplateService;
    }
}
