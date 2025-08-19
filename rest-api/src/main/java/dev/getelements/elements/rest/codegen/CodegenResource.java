package dev.getelements.elements.rest.codegen;

import com.fasterxml.jackson.databind.SerializationFeature;
import dev.getelements.elements.rest.Oas3DocumentationResource;
import dev.getelements.elements.sdk.model.Headers;
import dev.getelements.elements.sdk.model.codegen.CodegenRequest;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.codegen.CodegenService;
import dev.getelements.elements.sdk.service.version.VersionService;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import dev.getelements.elements.sdk.util.security.AuthorizationHeader;
import io.swagger.util.Json;
import io.swagger.v3.jaxrs2.integration.resources.BaseOpenApiResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import jakarta.inject.Inject;
import jakarta.servlet.ServletConfig;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.*;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.HashMap;

import static dev.getelements.elements.sdk.jakarta.rs.AuthSchemes.AUTH_BEARER;
import static dev.getelements.elements.sdk.jakarta.rs.AuthSchemes.SESSION_SECRET;
import static io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER;
import static io.swagger.v3.oas.models.security.SecurityScheme.Type.APIKEY;


@Path("codegen")
public class CodegenResource extends BaseOpenApiResource {

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(CodegenResource.class);

    private CodegenService codegenService;

    private ValidationHelper validationHelper;

    private VersionService versionService;

    private Oas3DocumentationResource documentationResource;

    private Client client;

    @POST
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(
            summary = "Generate Client API Code",
            description = "Generates API code for use on the client. " +
                    "Will generate Elements core if no application is specified in the request body.")
    public Response generateCode(final CodegenRequest request,
                                 @Context
                                 final UriInfo uriInfo,
                                 @Context
                                 final HttpHeaders headers,
                                 @Context
                                 final Application application,
                                 @Context
                                 final ServletConfig servletConfig
    ) {

        getValidationHelper().validateModel(request);

        final var generateCore = request.elementSpecUrl == null || request.elementSpecUrl.isEmpty();

        final var path = temporaryFiles.createTempDirectory().toFile();

        try {
            final var specFile = generateCore ?
                    generateSpecFile(path, "json", uriInfo, headers, application, servletConfig) :
                    generateElementSpecFile(path, request.elementSpecUrl);

            final var zipFile = getFileForRequest(request, specFile);
            final var bytes = FileUtils.readFileToByteArray(zipFile);

            temporaryFiles.deleteTempFilesAndDirectories();

            return Response.ok(bytes, MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + zipFile.getName() + "\"" )
                    .build();

        } catch (OpenApiConfigurationException | IOException e) {
            throw new InternalException(e);
        }
    }

    private File generateElementSpecFile(final File path, final String elementSpecUrl) {

        final OpenAPI openApi = getClient()
                .target(elementSpecUrl)
                .request("application/json")
                .get(OpenAPI.class);

        addSecuritySchemes(openApi);
        final var specFile = new File(path, "openapi.json");

        try(final var writer = new FileOutputStream(specFile)) {
            Json.mapper()
                    .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
                    .writeValue(writer, openApi);
        } catch (IOException e) {
            throw new InternalException(e);
        }

        return specFile;
    }

    private File generateSpecFile(final File path,
            final String type,
            final UriInfo uriInfo,
            final HttpHeaders headers,
            final Application application,
            final ServletConfig servletConfig) throws OpenApiConfigurationException {

        final var openApi = getDocumentationResource().getOpenApiJson(type, uriInfo, headers, application, servletConfig);
        addSecuritySchemes(openApi);
        final var specFile = new File(path, "openapi.json");

        try(final var writer = new FileOutputStream(specFile)) {
            Json.mapper()
                .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
                .writeValue(writer, openApi);
        } catch (IOException e) {
            throw new InternalException(e);
        }

        return specFile;
    }

    private File getFileForRequest(final CodegenRequest request, final File specFile) {

        return getCodegenService().generateCore(specFile, request.language, request.packageName, request.options);
    }

    private void addSecuritySchemes(final OpenAPI openApi) {

        final var components = openApi.getComponents() == null ? new Components() : openApi.getComponents();
        final var securitySchemes = components.getSecuritySchemes() == null || components.getSecuritySchemes().isEmpty() ?
                new HashMap<String, SecurityScheme>() : components.getSecuritySchemes();

        final var authBearerScheme = new SecurityScheme();
        authBearerScheme.setType(APIKEY);
        authBearerScheme.setIn(HEADER);
        authBearerScheme.setName(AuthorizationHeader.AUTH_HEADER);
        securitySchemes.put(AUTH_BEARER, authBearerScheme);

        final var sessionSecretScheme = new SecurityScheme();
        sessionSecretScheme.setType(APIKEY);
        sessionSecretScheme.setIn(HEADER);
        sessionSecretScheme.setName(Headers.SESSION_SECRET);
        securitySchemes.put(SESSION_SECRET, sessionSecretScheme);

        components.setSecuritySchemes(securitySchemes);
        openApi.setComponents(components);
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

    public Oas3DocumentationResource getDocumentationResource() {
        return documentationResource;
    }

    @Inject
    public void setDocumentationResource(Oas3DocumentationResource documentationResource) {
        this.documentationResource = documentationResource;
    }

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

}
