package com.namazustudios.socialengine.rest.formidium;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.formidium.FormidiumInvestor;
import com.namazustudios.socialengine.service.formidium.FormidiumService;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@Path("kyc/formidium")
@Api(
    value = "Followers",
    description = "Manages follower relationships among profiles.",
    authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)}
)
public class FormidiumResource {

    private FormidiumService formidiumService;

    private ValidationHelper validationHelper;

    @POST
    @Consumes(MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Creates a Formidium Investor",
            notes = "Creates a Formidium User in both the Elements database as well as makes the call to Formidium to" +
                    "create a new Investor. This accepts multipart, per the Formidium specification, and relays " +
                    "it directly the Formidium API. Refer to the Add Investor API in Formidium."
    )
    public FormidiumInvestor createFormidiumInvestor(final List<Map<String, Object>> multiPartFormData) {

        final var userId = multiPartFormData.stream()
                .filter(this::filterUserId)
                .map(m -> m.get("entity").toString())
                .findFirst()
                .orElse(null);

        final var filteredMultipartFormData = multiPartFormData
                .stream()
                .filter(m -> !filterUserId(m))
                .collect(toList());

        return getFormidiumService().createFormidiumInvestor(userId, filteredMultipartFormData);

    }

    private boolean filterUserId(final Map<String, Object> stringObjectMap) {

        // Discard form entries which do not have an entity.
        if (!stringObjectMap.containsKey("entity") || !stringObjectMap.containsKey("disposition"))
            return false;

        // Discard content which does not have a disposition either
        final var disposition = stringObjectMap.get("disposition").toString();

        if (disposition.isBlank())
            return false;

        final var parameters = Arrays
            .stream(disposition.split(";"))
            .map(String::trim)
            .toArray(String[]::new);

        if (!parameters[0].equalsIgnoreCase("form-data"))
            return false;

        return Arrays
            .stream(parameters)
            .anyMatch(p -> p.matches("name=\"elements_user_id\""));

    }

    @GET
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Gets Formidium Investors",
            notes = "Gets all Formidium investors available to the currently logged-in user."
    )
    public Pagination<FormidiumInvestor> getFormidiumInvestors(
            @QueryParam("userId") final String userId,
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count) {
        return getFormidiumService().getFormidiumInvestors(userId, offset, count);
    }

    @GET
    @Path("{formidiumInvestorId}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Gets a Specific Formidium Investor",
            notes = "Gets the specific Formidium investor, presuming that the investor is available to the currently " +
                    "logged-in user."
    )
    public FormidiumInvestor getFormidiumInvestor(final @PathParam("formidiumInvestorId") String formidiumInvestorId) {
        return getFormidiumService().getFormidiumInvestor(formidiumInvestorId);
    }

    @DELETE
    @Path("{formidiumInvestorId}")
    @ApiOperation(
            value = "Deletes a Specific Formidium Investor",
            notes = "Deletes the specific Formidium investor, presuming that the investor is available to the " +
                    "currently logged-in user."
    )
    public void deleteFormidiumInvestor(final @PathParam("formidiumInvestorId") String formidiumInvestorId) {
        getFormidiumService().deleteFormidiumInvestor(formidiumInvestorId);
    }

    public FormidiumService getFormidiumService() {
        return formidiumService;
    }

    @Inject
    public void setFormidiumService(final FormidiumService formidiumService) {
        this.formidiumService = formidiumService;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
