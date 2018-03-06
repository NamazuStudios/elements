package com.namazustudios.socialengine.rest.swagger;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.Headers;
import com.namazustudios.socialengine.service.ApplicationService;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static io.swagger.models.Scheme.forValue;
import static java.util.Arrays.asList;

/**
 * Created by patricktwohig on 7/14/17.
 */
@SwaggerDefinition(
    securityDefinition = @SecurityDefinition(
        apiKeyAuthDefinitions = {@ApiKeyAuthDefinition(
            name = SESSION_SECRET,
            description = "Uses a server-assigned session key which is generated from various POST /session and " +
                          "POST /facebook_session endpoints in the API.",
            in = ApiKeyAuthDefinition.ApiKeyLocation.HEADER,
            key = EnhancedApiListingResource.SESSION_SECRET
        )}
    )
)
public class EnhancedApiListingResource extends ApiListingResource {

    public static final String SESSION_SECRET = "session_secret";

    private URI apiOutsideUrl;

    private ApplicationService applicationService;

    @Override
    protected Swagger process(Application app, ServletContext servletContext, ServletConfig sc, HttpHeaders headers, UriInfo uriInfo) {
        final Swagger swagger = super.process(app, servletContext, sc, headers, uriInfo);
        appendHostInformation(swagger);
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

}
