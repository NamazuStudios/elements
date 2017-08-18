package com.namazustudios.socialengine.rest.swagger;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.rt.ManifestLoader;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.http.HttpModule;
import com.namazustudios.socialengine.rt.manifest.http.HttpOperation;
import com.namazustudios.socialengine.rt.manifest.model.Model;
import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;
import com.namazustudios.socialengine.rt.manifest.model.Property;
import com.namazustudios.socialengine.service.ApplicationService;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.models.ModelImpl;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.properties.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.collect.Streams.stream;
import static io.swagger.models.Scheme.forValue;
import static java.util.Arrays.asList;

/**
 * Created by patricktwohig on 7/14/17.
 */
@SwaggerDefinition(
    securityDefinition = @SecurityDefinition(
        apiKeyAuthDefinitions = {@ApiKeyAuthDefinition(
            name = "Authorization",
            description = "Uses a combination Facebook Application ID in combination with an OAuth Token " +
                          "in order to perform API operations.  Must be specified in the format Facebook " +
                          "Authorization Facebook appid:token.  Failure to specify both app ID and token " +
                          "will result in a failed request.",
            in = ApiKeyAuthDefinition.ApiKeyLocation.HEADER,
            key = EnhancedApiListingResource.FACBOOK_OAUTH_KEY
        )}
    )
)
public class EnhancedApiListingResource extends ApiListingResource {

    public static final String FACBOOK_OAUTH_KEY = "facebook_oauth";

    private URI apiOutsideUrl;

    private ApplicationService applicationService;

    private Function<com.namazustudios.socialengine.model.application.Application, ManifestLoader> applicationManifestLoaderFunction;

    @Override
    protected Swagger process(Application app, ServletContext servletContext, ServletConfig sc, HttpHeaders headers, UriInfo uriInfo) {
        final Swagger swagger = super.process(app, servletContext, sc, headers, uriInfo);
        appendHostInformation(swagger);
        appendManifests(swagger);
        return swagger;
    }

    private void appendHostInformation(final Swagger swagger) {

        final StringBuilder hostStringBuilder = new StringBuilder();

        if (getApiOutsideUrl().getUserInfo() != null) {
            hostStringBuilder
                .append(getApiOutsideUrl().getUserInfo())
                .append("@");
        }

        hostStringBuilder.append(getApiOutsideUrl().getHost());

        if (getApiOutsideUrl().getPort() >= 0) {
            hostStringBuilder
                    .append(":")
                    .append(getApiOutsideUrl().getPort());
        }

        swagger.setHost(hostStringBuilder.toString());
        swagger.setBasePath(getApiOutsideUrl().getPath());

        final Scheme scheme = forValue(getApiOutsideUrl().getScheme());

        if (scheme != null) {
            swagger.setSchemes(asList(scheme));
        }

    }

    private void appendManifests(final Swagger swagger) {
        stream(getApplicationService().getApplications())
            .forEach(app -> appendManifests(swagger, app));
    }

    private void appendManifests(
            final Swagger swagger,
            final com.namazustudios.socialengine.model.application.Application application) {

        // Gets the loader for the application

        final ManifestLoader manifestLoader = getApplicationManifestLoaderFunction().apply(application);

        final ModelManifest modelManifest = manifestLoader.getModelManifest();
        appendModelManifest(swagger, modelManifest);

        final HttpManifest httpManifest = manifestLoader.getHttpManifest();
        appendHttpManifest(swagger, httpManifest, modelManifest);

    }

    private void appendModelManifest(final Swagger swagger, final ModelManifest modelManifest) {
        for (final Model model : modelManifest.getModelsByName().values()) {
            final ModelImpl swaggerModel = new ModelImpl();
            swaggerModel.setName(model.getName());
            swaggerModel.setDescription(model.getDescription());
            model.getProperties().forEach((name, property) -> swaggerModel.addProperty(name, toSwaggerProperty(property)));
            swagger.addDefinition(model.getName(), swaggerModel);
        }
    }

    private io.swagger.models.properties.Property toSwaggerProperty(final Property property) {
        switch (property.getType()) {
            case NUMBER:
                return new DoubleProperty().description(property.getDescription());
            case STRING:
                return new StringProperty().description(property.getDescription());
            case BOOLEAN:
                return new BooleanProperty().description(property.getDescription());
            case ARRAY:
                return new ArrayProperty(new RefProperty(property.getModel())).description(property.getDescription());
            case OBJECT:
                return new RefProperty(property.getModel()).description(property.getDescription());
            default:
                throw new IllegalArgumentException("Unsupported property type: " + property.getType());
        }
    }

    private void appendHttpManifest(final Swagger swagger,
                                    final HttpManifest httpManifest,
                                    final ModelManifest modelManifest) {

        for (final HttpModule httpModule : httpManifest.getModulesByName().values()) {

            final Map<String, HttpOperation> httpOperationsByName = httpModule.getOperationsByName();

            for (final HttpOperation httpOperation : httpOperationsByName.values()) {

            }

        }
    }

    public URI getApiOutsideUrl() {
        return apiOutsideUrl;
    }

    @Inject
    public void setApiOutsideUrl(@Named(Constants.API_OUTSIDE_URL) URI apiOutsideUrl) {
        this.apiOutsideUrl = apiOutsideUrl;
    }

    public ApplicationService getApplicationService() {
        return applicationService;
    }

    @Inject
    public void setApplicationService(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public Function<com.namazustudios.socialengine.model.application.Application, ManifestLoader> getApplicationManifestLoaderFunction() {
        return applicationManifestLoaderFunction;
    }

    @Inject
    public void setApplicationManifestLoaderFunction(Function<com.namazustudios.socialengine.model.application.Application, ManifestLoader> applicationManifestLoaderFunction) {
        this.applicationManifestLoaderFunction = applicationManifestLoaderFunction;
    }

}
