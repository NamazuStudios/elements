package com.namazustudios.socialengine.rest.application;

import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource;
import com.namazustudios.socialengine.rt.ParameterizedPath;
import com.namazustudios.socialengine.rt.manifest.Header;
import com.namazustudios.socialengine.rt.manifest.http.*;
import com.namazustudios.socialengine.rt.manifest.model.Model;
import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;
import com.namazustudios.socialengine.rt.manifest.model.Property;
import com.namazustudios.socialengine.rt.manifest.security.AuthScheme;
import com.namazustudios.socialengine.rt.manifest.security.SecurityManifest;
import com.namazustudios.socialengine.service.ApplicationService;
import com.namazustudios.socialengine.service.ManifestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.models.*;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.*;
import org.glassfish.jersey.internal.util.Producer;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.google.common.base.Functions.identity;
import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;
import static io.swagger.models.Scheme.forValue;
import static io.swagger.models.auth.In.HEADER;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * Created by patricktwohig on 8/23/17.
 */
@Api(value = "Application Documentation",
        description =
                "Manages application documentation.  This generates Swagger JSON from the application's configured " +
                "manifest such that it may be used to generate client side code or simply browse and test the " +
                "application's endpoints.",
        authorizations = {@Authorization(AUTH_BEARER), @Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("application/{applicationNameOrId}/swagger.json")
public class ApplicationDocumentationResource {

    private ManifestService manifestService;

    private ApplicationService applicationService;

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "The swagger definition in either JSON or YAML", hidden = true)
    public Swagger getJsonDocumentation(@PathParam("applicationNameOrId") final String applicationNameOrId) {
        final Swagger swagger = generateSwagger(applicationNameOrId);
        return swagger;
    }

    private Swagger generateSwagger(final String applicationNameOrId) {

        final Swagger swagger = new Swagger();
        final Application application = getApplicationService().getApplication(applicationNameOrId);

        if (application.getHttpTunnelEndpointUrl() == null) {
            throw new NotFoundException();
        }

        appendHostInformation(swagger, application);
        appendHttpManifests(swagger, application);

        return swagger;

    }

    private void appendHostInformation(final Swagger swagger, final Application application) {

        final URI uri;

        try {
            uri = new URI(application.getHttpTunnelEndpointUrl());
        } catch (URISyntaxException ex) {
            throw new InternalException(ex);
        }

        final StringBuilder hostStringBuilder = new StringBuilder();

        if (uri.getUserInfo() != null) {
            hostStringBuilder
                    .append(uri.getUserInfo())
                    .append("@");
        }

        hostStringBuilder.append(uri.getHost());

        if (uri.getPort() >= 0) {
            hostStringBuilder
                    .append(":")
                    .append(uri.getPort());
        }

        swagger.setHost(hostStringBuilder.toString());
        swagger.setBasePath(uri.getPath());

        final Scheme scheme = forValue(uri.getScheme());

        if (scheme != null) {
            swagger.setSchemes(asList(scheme));
        }

    }

    private void appendHttpManifests(final Swagger swagger, final Application application) {

        final ModelManifest modelManifest = getManifestService().getModelManifestForApplication(application);
        appendModelManifest(swagger, modelManifest, application);

        final HttpManifest httpManifest = getManifestService().getHttpManifestForApplication(application);
        appendHttpManifest(swagger, httpManifest);

        final SecurityManifest securityManifest = getManifestService().getSecurityManifestForApplication(application);
        appendSecurityManifest(swagger, securityManifest);

    }

    private void appendModelManifest(final Swagger swagger, final ModelManifest modelManifest, Application application) {
        for (final Model model : modelManifest.getModelsByName().values()) {
            final ModelImpl swaggerModel = new ModelImpl();
            swaggerModel.setName(model.getName());
            swaggerModel.setTitle(model.getName());
            swaggerModel.setDescription(model.getDescription());
            model.getProperties().forEach((name, property) -> swaggerModel.addProperty(name, toSwaggerProperty(property)));
            swagger.addDefinition(model.getName(), swaggerModel);
        }
    }

    private io.swagger.models.properties.Property toSwaggerProperty(final Property property) {
        switch (property.getType()) {
            case NUMBER:
                return new DoubleProperty().description(property.getDescription());
            case INTEGER:
                return new IntegerProperty().description(property.getDescription());
            case STRING:
                return new StringProperty().description(property.getDescription());
            case BOOLEAN:
                return new BooleanProperty().description(property.getDescription());
            case ARRAY:
                return new ArrayProperty().items(new RefProperty(property.getModel())).description(property.getDescription());
            case OBJECT:
                return new RefProperty().asDefault(property.getModel()).description(property.getDescription());
            default:
                throw new IllegalArgumentException("Unsupported property type: " + property.getType());
        }
    }

    private io.swagger.models.properties.Property toSwaggerProperty(final Header header) {
        switch (header.getType()) {
            case NUMBER:
                return new DoubleProperty().description(header.getDescription());
            case INTEGER:
                return new IntegerProperty().description(header.getDescription());
            case STRING:
                return new StringProperty().description(header.getDescription());
            case BOOLEAN:
                return new BooleanProperty().description(header.getDescription());
            default:
                throw new IllegalArgumentException("Unsupported header type: " + header.getType());
        }
    }

    private void appendHttpManifest(final Swagger swagger,
                                    final HttpManifest httpManifest) {

        final Map<ParameterizedPath, io.swagger.models.Path> parameterizedPathPathMap = new HashMap<>();

        for (final HttpModule httpModule : httpManifest.getModulesByName().values()) {

            final Map<String, HttpOperation> httpOperationsByName = httpModule.getOperationsByName();

            for (final HttpOperation httpOperation : httpOperationsByName.values()) {
                final io.swagger.models.Path path = parameterizedPathPathMap.computeIfAbsent(httpOperation.getPath(), this::computePath);
                resolveOperation(swagger, httpOperation, path);
            }

        }

        parameterizedPathPathMap.forEach((parameterizedPath, swaggerPath) -> {
            final String pathKey = parameterizedPath.getRaw().toAbsolutePathString();
            swagger.path(pathKey, swaggerPath);
        });

    }

    private io.swagger.models.Path computePath(final ParameterizedPath parameterizedPath) {

        final io.swagger.models.Path computed = new io.swagger.models.Path();

        parameterizedPath.getParameters().forEach(parameter -> {
            final PathParameter pathParameter = new PathParameter();
            pathParameter.setName(parameter);
            computed.addParameter(pathParameter);
        });

        return computed;

    }

    private void resolveOperation(final Swagger swagger,
                                  final HttpOperation httpOperation,
                                  final io.swagger.models.Path path) {
        switch (httpOperation.getVerb()) {
            case GET:
                resolveOperation(swagger, httpOperation, path::getGet, path::get);
                break;
            case PUT:
                resolveOperation(swagger, httpOperation, path::getPut, path::put);
                break;
            case HEAD:
                resolveOperation(swagger, httpOperation, path::getHead, path::head);
                break;
            case POST:
                resolveOperation(swagger, httpOperation, path::getPost, path::post);
                break;
            case DELETE:
                resolveOperation(swagger, httpOperation, path::getDelete, path::delete);
                break;
            case OPTIONS:
                resolveOperation(swagger, httpOperation, path::getOptions, path::options);
                break;
            default:
                throw new InvalidDataException("Invalid HTTP verb" + httpOperation.getVerb());
        }
    }

    private void resolveOperation(final Swagger swagger,
                                  final HttpOperation httpOperation,
                                  final Producer<Operation> operationProducer,
                                  final Consumer<Operation> operationConsumer) {

        final Operation existing = operationProducer.call();

        if (existing != null) {
            throw new InvalidDataException("HandlerOperation already defined " + httpOperation.getVerb() + " " + httpOperation.getPath().getRaw().toAbsolutePathString());
        }

        final List<Parameter> parameters = resolveParameters(httpOperation);
        final List<String> consumes = new ArrayList<>(httpOperation.getConsumesContentByType().keySet());
        final List<String> produces = new ArrayList<>(httpOperation.getProducesContentByType().keySet());
        final List<Response> responses = resolveResponses(httpOperation.getProducesContentByType().values());

        final Operation operation = new Operation();

        operation.setConsumes(consumes);
        operation.setProduces(produces);
        operation.setParameters(parameters);
        operation.setOperationId(httpOperation.getName());
        operation.setDescription(httpOperation.getDescription());

        if (httpOperation.getAuthSchemes() != null) {
            httpOperation.getAuthSchemes().forEach(scheme -> operation.addSecurity(scheme, emptyList()));
        }

        final Parameter bodyParameter = resolveBodyParameter(swagger, httpOperation);

        if (bodyParameter != null) {
            operation.addParameter(bodyParameter);
        }

        operation.setResponses(responses.stream().collect(Collectors.toMap(r -> "200", identity())));
        operationConsumer.accept(operation);

    }

    private List<Parameter> resolveParameters(final HttpOperation httpOperation) {
        final List<Parameter> parameters = new ArrayList<>();

        httpOperation.getProducesContentByType()
            .values()
            .stream()
            .flatMap(c -> c.getHeaders().entrySet().stream())
            .map(e -> {
                final HeaderParameter parameter = new HeaderParameter();
                parameter.setName(e.getKey());
                parameter.setProperty(toSwaggerProperty(e.getValue()));
                return parameter;
            }).forEach(parameters::add);

        httpOperation.getParameters()
                .entrySet()
                .stream()
                .map(entry -> {
                    final QueryParameter parameter = new QueryParameter();
                    parameter.setName(entry.getKey());
                    parameter.setType(entry.getValue().getType().name());
                    return parameter;
                }).forEach(parameters::add);

        return parameters;

    }

    private Parameter resolveBodyParameter(final Swagger swagger, final HttpOperation httpOperation) {

        final Map<String, io.swagger.models.Model> swaggerDefinitions = swagger.getDefinitions();

        if (swaggerDefinitions == null) {
            return null;
        }

        final Map<String, HttpContent> consumesContentByType =  httpOperation.getConsumesContentByType();

        return consumesContentByType
            .values().stream()
            .filter(c -> c != null).map(c -> c.getModel())
            .filter(m -> m != null).map(m -> new BodyParameter().schema(new RefModel(m)))
            .findFirst().orElse(null);

    }

    private List<Response> resolveResponses(final Collection<HttpContent> httpContentCollection) {
        return httpContentCollection.stream()
            .map(this::resolveResponse)
            .collect(Collectors.toList());
    }

    private Response resolveResponse(final HttpContent content) {

        final Map<String, io.swagger.models.properties.Property> headerPropertyMap = content
            .getHeaders()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(e -> e.getKey(), e -> toSwaggerProperty(e.getValue())));

        final Response response = new Response();
        final RefProperty type = new RefProperty(content.getModel());
        response.setSchema(type);
        response.setHeaders(headerPropertyMap);
        return response;

    }

    private void appendSecurityManifest(final Swagger swagger, final SecurityManifest securityManifest) {
        securityManifest
            .getHeaderAuthSchemesByName()
            .forEach((name, header) -> addHeaderAuth(swagger, name, header));
    }

    private void addHeaderAuth(final Swagger swagger, final String name, final AuthScheme.Header headerAuthScheme) {

        final Header headerSpec = headerAuthScheme.getSpec();

        final ApiKeyAuthDefinition apiKeyAuthDefinition = new ApiKeyAuthDefinition()
                .name(headerSpec.getName())
                .in(HEADER);

        apiKeyAuthDefinition.setDescription(headerAuthScheme.getDescription());
        swagger.addSecurityDefinition(name, apiKeyAuthDefinition);

    }

    public ManifestService getManifestService() {
        return manifestService;
    }

    @Inject
    public void setManifestService(ManifestService manifestService) {
        this.manifestService = manifestService;
    }

    public ApplicationService getApplicationService() {
        return applicationService;
    }

    @Inject
    public void setApplicationService(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

}
