package dev.getelements.elements.rest;

import com.fasterxml.jackson.databind.util.TokenBuffer;
import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.sdk.model.Constants;
import dev.getelements.elements.sdk.model.ErrorResponse;
import dev.getelements.elements.sdk.model.Headers;
import dev.getelements.elements.sdk.service.version.VersionService;
import dev.getelements.elements.sdk.util.security.AuthorizationHeader;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.core.filter.AbstractSpecFilter;
import io.swagger.v3.core.filter.OpenAPISpecFilter;
import io.swagger.v3.core.filter.SpecFilter;
import io.swagger.v3.core.model.ApiDescription;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.BaseOpenApiResource;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletConfig;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import static dev.getelements.elements.sdk.jakarta.rs.DefaultExceptionMapper.HTTP_STATUS_MAP;
import static dev.getelements.elements.sdk.jakarta.rs.AuthSchemes.AUTH_BEARER;
import static dev.getelements.elements.sdk.jakarta.rs.AuthSchemes.SESSION_SECRET;
import static io.swagger.v3.oas.annotations.enums.SecuritySchemeIn.HEADER;
import static io.swagger.v3.oas.annotations.enums.SecuritySchemeType.APIKEY;
import static io.swagger.v3.oas.models.Components.COMPONENTS_SCHEMAS_REF;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.lang.String.format;

@OpenAPIDefinition(
        info = @Info(
                title = "Namazu Elements",
                description = "Namazu Elements Core APIs",
                contact = @Contact(
                        url = "https://namazustudios.com",
                        email = "info@namazustudios.com",
                        name = "Namazu Studios / Elemental Computing Inc."
                )
        ),
        externalDocs = @ExternalDocumentation(
                url = "https://namazustudios.com/docs",
                description = "Please see the Namazu Elements Manual for more information."
        ),
        security = {
                @SecurityRequirement(name = AUTH_BEARER),
                @SecurityRequirement(name = SESSION_SECRET)
        }
)
@SecuritySchemes({
        @SecurityScheme(
                type = APIKEY,
                in = HEADER,
                name = AUTH_BEARER,
                paramName = AuthorizationHeader.AUTH_HEADER),
        @SecurityScheme(
                type = APIKEY,
                in = HEADER,
                name = SESSION_SECRET,
                paramName = Headers.SESSION_SECRET)
})
@Path("openapi.{type:json|yaml}")
public class Oas3DocumentationResource extends BaseOpenApiResource {

    private URI apiOutsideUrl;

    private VersionService versionService;

    private static final ResolvedSchema ERROR_RESPONSE_SCHEMA_3_0 = ModelConverters
            .getInstance(false)
            .readAllAsResolvedSchema(ErrorResponse.class);

    private static final ResolvedSchema ERROR_RESPONSE_SCHEMA_3_1 = ModelConverters
            .getInstance(true)
            .readAllAsResolvedSchema(ErrorResponse.class);

    @GET
    @Produces(APPLICATION_JSON)
    @Operation(hidden = true)
    public OpenAPI getOpenApiJson(
            @PathParam("type")
            final String type,
            @Context
            final UriInfo uriInfo,
            @Context
            final HttpHeaders headers,
            @Context
            final Application application,
            @Context
            final ServletConfig servletConfig
        ) throws OpenApiConfigurationException {
        return getOpenApi(type, uriInfo, headers, application, servletConfig);
    }

    @GET
    @Produces("application/yaml")
    @Operation(hidden = true)
    public OpenAPI getOpenApiYaml(
            @PathParam("type")
            final String type,
            @Context
            final UriInfo uriInfo,
            @Context
            final HttpHeaders headers,
            @Context
            final Application application,
            @Context
            final ServletConfig servletConfig
    ) throws OpenApiConfigurationException {
        return getOpenApi(type, uriInfo, headers, application, servletConfig);
    }

    private OpenAPI getOpenApi(
            final String type,
            final UriInfo uriInfo,
            final HttpHeaders headers,
            final Application application,
            final ServletConfig servletConfig
    ) throws OpenApiConfigurationException {

        final var resourcePackages = Set.of(
                "dev.getelements.elements.rest",
                "dev.getelements.elements.model"
        );

        final var context = new JaxrsOpenApiContextBuilder()
                .application(application)
                .servletConfig(servletConfig)
                .resourcePackages(resourcePackages)
                .openApiConfiguration(null)
                .ctxId(getContextId(servletConfig))
                .buildContext(true);

        final var oas = context.read();
        final var clone = cloneOas3Spec(type, context, oas);

        final OpenAPISpecFilter filter;

        switch (oas.getSpecVersion()) {
            case V30:
                filter = new EnhancedSpecFilter(ERROR_RESPONSE_SCHEMA_3_0);
                break;
            case V31:
                filter = new EnhancedSpecFilter(ERROR_RESPONSE_SCHEMA_3_1);
                break;
            default:
                throw new IllegalArgumentException("Unsupported spec version: " + oas.getPaths());
        }

        return new SpecFilter().filter(
                clone,
                filter,
                getQueryParams(uriInfo.getQueryParameters()),
                getCookies(headers),
                getHeaders(headers)
        );
    }

    private OpenAPI cloneOas3Spec(final String type, final OpenApiContext context, final OpenAPI oas) {

        final var mapper =
                "yaml".equals(type) ? context.getOutputYamlMapper() :
                "json".equals(type) ? context.getOutputJsonMapper() :
                null;

        if (mapper == null) {
            throw new IllegalArgumentException("Must be one of 'yaml' or 'json'");
        }

        try (final var buffer = new TokenBuffer(mapper, false)) {
            mapper.writeValue(buffer, oas);
            return mapper.readValue(buffer.asParser(), OpenAPI.class);
        } catch (IOException ex) {
            throw new InternalException(ex);
        }

    }

    public URI getApiOutsideUrl() {
        return apiOutsideUrl;
    }

    @Inject
    public void setApiOutsideUrl(@Named(Constants.API_OUTSIDE_URL) URI apiOutsideUrl) {
        this.apiOutsideUrl = apiOutsideUrl;
    }

    public VersionService getVersionService() {
        return versionService;
    }

    @Inject
    public void setVersionService(VersionService versionService) {
        this.versionService = versionService;
    }

    private static Map<String, List<String>> getQueryParams(MultivaluedMap<String, String> params) {
        Map<String, List<String>> output = new HashMap<>();
        if (params != null) {
            params.forEach(output::put);
        }
        return output;
    }

    private static Map<String, String> getCookies(HttpHeaders headers) {
        Map<String, String> output = new HashMap<>();
        if (headers != null) {
            headers.getCookies().forEach((k, v) -> output.put(k, v.getValue()));
        }
        return output;
    }

    private static Map<String, List<String>> getHeaders(HttpHeaders headers) {
        Map<String, List<String>> output = new HashMap<>();
        if (headers != null) {
            headers.getRequestHeaders().forEach(output::put);
        }
        return output;
    }

    private class EnhancedSpecFilter extends AbstractSpecFilter {

        private final ResolvedSchema errorResponseSchema;

        public EnhancedSpecFilter(final ResolvedSchema errorResponseSchema) {
            this.errorResponseSchema = errorResponseSchema;
        }

        @Override
        public Optional<OpenAPI> filterOpenAPI(
                final OpenAPI openAPI,
                final Map<String, List<String>> params,
                final Map<String, String> cookies,
                final Map<String, List<String>> headers) {

            final var server = new Server();
            server.setUrl(getApiOutsideUrl().toString());
            server.setDescription("Elements Core API");
            openAPI.addServersItem(server);

            final var components = openAPI.getComponents();
            components.addSchemas(errorResponseSchema.schema.getName(), errorResponseSchema.schema);


            final var info = openAPI.getInfo();
            info.setVersion(getVersionService().getVersion().toString());

            return Optional.of(openAPI);
        }

        @Override
        public Optional<io.swagger.v3.oas.models.Operation> filterOperation(
                final io.swagger.v3.oas.models.Operation operation,
                final ApiDescription api,
                final Map<String, List<String>> params,
                final Map<String, String> cookies,
                final Map<String, List<String>> headers) {

            final ApiResponses responses;

            if (operation.getResponses() == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            } else {
                responses = operation.getResponses();
            }

            HTTP_STATUS_MAP.values().forEach(code -> {

                var $ref = format("%s%s",COMPONENTS_SCHEMAS_REF, errorResponseSchema.schema.getName());

                var schema = new io.swagger.v3.oas.models.media.Schema<>().$ref($ref);

                var content = new Content().addMediaType(
                        APPLICATION_JSON.toLowerCase(),
                        new io.swagger.v3.oas.models.media.MediaType().schema(schema)
                );

                var response = new ApiResponse().content(content);
                responses.addApiResponse(format("%d", code.getStatusCode()), response);

            });

            return super.filterOperation(operation, api, params, cookies, headers);

        }

    }

}
