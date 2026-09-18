package dev.getelements.elements.rest.schema.email;

import com.google.common.base.Strings;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.schema.email.CreateEmailTemplateRequest;
import dev.getelements.elements.sdk.model.schema.email.EmailTemplate;
import dev.getelements.elements.sdk.model.schema.email.UpdateEmailTemplateRequest;
import dev.getelements.elements.sdk.service.schema.email.EmailTemplateService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/email_template")
public class EmailTemplateResource {

    private EmailTemplateService emailTemplateService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get Email Templates",
            description = "Gets a pagination of Email Templates for the given query.")
    public Pagination<EmailTemplate> getEmailTemplates(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count) {
        return getEmailTemplateService().getEmailTemplates(offset, count);
    }

    @GET
    @Path("{emailTemplateIdOrKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a specific Email Template",
            description = "Gets a specific EmailTemplate by id or key.")
    public EmailTemplate getEmailTemplate(@PathParam("emailTemplateIdOrKey") String emailTemplateIdOrKey) {
        return getEmailTemplateService().getEmailTemplate(emailTemplateIdOrKey);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Creates a new Email Template",
            description = "Creates a new Email Template.")
    public EmailTemplate createEmailTemplate(final CreateEmailTemplateRequest createEmailTemplateRequest) {
        return getEmailTemplateService().createEmailTemplate(createEmailTemplateRequest);
    }

    @PUT
    @Path("{emailTemplateId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Updates an Email Template",
            description = "Updates an EmailTemplate with the specified id.")
    public EmailTemplate updateEmailTemplate(
            @PathParam("emailTemplateId") String emailTemplateId,
            final UpdateEmailTemplateRequest updateEmailTemplateRequest) {
        return getEmailTemplateService().updateEmailTemplate(emailTemplateId, updateEmailTemplateRequest);
    }

    @DELETE
    @Path("{emailTemplateId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Deletes an Email Template",
            description = "Deletes an EmailTemplate with the specified id.")
    public void deleteEmailTemplate(@PathParam("emailTemplateId") String emailTemplateId) {

        emailTemplateId = Strings.nullToEmpty(emailTemplateId).trim();

        if (emailTemplateId.isEmpty()) {
            throw new NotFoundException();
        }

        getEmailTemplateService().deleteEmailTemplate(emailTemplateId);
    }

    public EmailTemplateService getEmailTemplateService() {
        return emailTemplateService;
    }

    @Inject
    public void setEmailTemplateService(EmailTemplateService emailTemplateService) {
        this.emailTemplateService = emailTemplateService;
    }

}
