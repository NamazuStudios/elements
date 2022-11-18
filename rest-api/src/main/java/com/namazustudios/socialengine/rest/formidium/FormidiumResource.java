package com.namazustudios.socialengine.rest.formidium;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.formidium.CreateFormidiumInvestorRequest;
import com.namazustudios.socialengine.model.formidium.FormidiumInvestor;
import com.namazustudios.socialengine.service.formidium.FormidiumService;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Form;
import java.util.Map;
import java.util.stream.Collectors;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("kyc/formidium")
@Api(
    value = "Followers",
    description = "Manages follower relationships among profiles.",
    authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)}
)
public class FormidiumResource {

    private FormidiumService formidiumService;

    public static final String ELEMENTS_USER_ID_FORM_PARAM = "elements_user_id";

    private ValidationHelper validationHelper;

    @POST
    @Consumes(APPLICATION_FORM_URLENCODED)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Creates a Formidium Investor",
            notes = "Creates a Formidium User in both the Elements database as well as makes the call to Formidium to" +
                    "create a new Investor."
    )
    public FormidiumInvestor createFormidiumInvestorForm(final Form form) {

        final var userId = form.asMap().getFirst(ELEMENTS_USER_ID_FORM_PARAM);

        final var params = form
            .asMap()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                final var value = entry.getValue();
                return value.size() == 1 ? value.get(0) : value;
            }));

        final var createFormidiumInvestorRequest = new CreateFormidiumInvestorRequest();
        createFormidiumInvestorRequest.setUserId(userId);
        createFormidiumInvestorRequest.setFormidiumApiParameters(params);

        return createFormidiumInvestorJson(createFormidiumInvestorRequest);

    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Creates a Formidium Investor",
            notes = "Creates a Formidium User in both the Elements database as well as makes the call to Formidium to" +
                    "create a new Investor."
    )
    public FormidiumInvestor createFormidiumInvestorJson(final CreateFormidiumInvestorRequest createFormidiumInvestorRequest) {
        getValidationHelper().validateModel(createFormidiumInvestorRequest);
        return getFormidiumService().createFormidiumInvestor(createFormidiumInvestorRequest);
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
