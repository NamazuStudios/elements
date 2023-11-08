package dev.getelements.elements.rest.formidium;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.formidium.FormidiumInvestor;
import dev.getelements.elements.rest.AuthSchemes;
import dev.getelements.elements.service.formidium.FormidiumService;
import dev.getelements.elements.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static dev.getelements.elements.Headers.USER_AGENT;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;

@Path("kyc/formidium")
@Api(
    value = "Formidium",
    description = "Manages follower relationships among profiles.",
    authorizations = {@Authorization(AuthSchemes.AUTH_BEARER), @Authorization(AuthSchemes.SESSION_SECRET), @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)}
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
    public FormidiumInvestor createFormidiumInvestor(
            @HeaderParam(USER_AGENT)
            final String userAgent,
            final List<Map<String, Object>> multiPartFormData) {

        final var userId = multiPartFormData.stream()
                .filter(this::filterUserId)
                .map(m -> (Function<Class<?>, Object>) m.get("reader"))
                .map(f -> f.apply(String.class).toString())
                .findFirst()
                .orElse(null);

        final var filteredMultipartFormData = multiPartFormData
                .stream()
                .filter(m -> !filterUserId(m))
                .collect(toList());

        return getFormidiumService().createFormidiumInvestor(userId, userAgent, filteredMultipartFormData);

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
    @Produces(APPLICATION_JSON)
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
