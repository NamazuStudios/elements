package dev.getelements.elements.rest.codegen;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.codegen.CodegenRequest;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.codegen.CodegenService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.io.File;

import static com.google.common.base.Strings.isNullOrEmpty;

@Path("codegen")
public class CodegenResource {

    private CodegenService codegenService;

    private ValidationHelper validationHelper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Generate Client API Code",
            description = "Generates API (Elements core if no application is specified) code for use on the client.")
    public File generateCode(CodegenRequest request) {

        getValidationHelper().validateModel(request);

        if(isNullOrEmpty(request.applicationNameOrId)) {
            return getCodegenService().generateCore(request.language, request.options);
        }

        return getCodegenService().generateApplication(request.applicationNameOrId, request.language, request.options);
    }


    public CodegenService getCodegenService() {
        return codegenService;
    }

    @Inject
    public void setCodegenService(CodegenService codegenService) {
        this.codegenService = codegenService;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }
}
