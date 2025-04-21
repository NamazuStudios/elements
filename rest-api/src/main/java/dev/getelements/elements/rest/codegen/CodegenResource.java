package dev.getelements.elements.rest.codegen;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.getelements.elements.rest.Oas3DocumentationResource;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.codegen.CodegenRequest;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.codegen.CodegenService;
import dev.getelements.elements.sdk.service.version.VersionService;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import io.swagger.util.Json;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.inject.Inject;
import jakarta.servlet.ServletConfig;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.*;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.UUID;

import static com.google.common.base.Strings.isNullOrEmpty;

@Path("codegen")
public class CodegenResource {

    private CodegenService codegenService;

    private ValidationHelper validationHelper;

    private VersionService versionService;

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(
            summary = "Generate Client API Code",
            description = "Generates API (Elements core if no application is specified) code for use on the client.")
    public Response generateCode(
            final CodegenRequest request,
            @Context
            final UriInfo uriInfo,
            @Context
            final HttpHeaders headers,
            @Context
            final Application application,
            @Context
            final ServletConfig servletConfig
    ) throws OpenApiConfigurationException, IOException {

        getValidationHelper().validateModel(request);

        //create temp directory
        //create temp file
        //get json
        //pass file ref to gen
        //create temp directory within the file's temp directory
        //create generated code in temp subdirectory
        //zip up subdirectory into original file ref
        //copy file to bytes√
        //delete temp directories + files √
        //return bytes in response√

        final var tempFolderName = "codegen-" + UUID.randomUUID();
        final var temporaryFiles = new TemporaryFiles(tempFolderName);
        final var path = temporaryFiles.createTempDirectory();

        final var specFile = generateSpecFile(path.toFile(), "json", uriInfo, headers, application, servletConfig);
        final var zipFile = getFileForRequest(request, specFile);
        final var bytes = FileUtils.readFileToByteArray(zipFile);

        temporaryFiles.deleteTempFilesAndDirectories();

        return Response.ok(bytes, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + zipFile.getName() + "\"" ) //optional
                .build();
    }

    private File generateSpecFile(final File path,
            final String type,
            final UriInfo uriInfo,
            final HttpHeaders headers,
            final Application application,
            final ServletConfig servletConfig) throws OpenApiConfigurationException {

        final Oas3DocumentationResource oas3Resource = new Oas3DocumentationResource();
        //These are not injected when called from a separate resource
        oas3Resource.setApiOutsideUrl(uriInfo.getBaseUri());
        oas3Resource.setVersionService(getVersionService());

        final var openApi = oas3Resource.getOpenApiJson(type, uriInfo, headers, application, servletConfig);
        final var specFile = new File(path, "openapi.json");

        try(final var writer = new FileOutputStream(specFile)) {
            final var json = Json.mapper().writeValueAsString(openApi);
            writer.write(json.getBytes());
        } catch (IOException e) {
            throw new InternalException(e);
        }

        return specFile;
    }

    private File getFileForRequest(final CodegenRequest request, final File specFile) {

        if(request.applicationNameOrId == null || request.applicationNameOrId.isEmpty()) {
            return getCodegenService().generateCore(specFile, request.language, request.options);
        }

        return getCodegenService().generateApplication(specFile, request.applicationNameOrId, request.language, request.options);
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

    public VersionService getVersionService() {
        return versionService;
    }

    @Inject
    public void setVersionService(VersionService versionService) {
        this.versionService = versionService;
    }
}
