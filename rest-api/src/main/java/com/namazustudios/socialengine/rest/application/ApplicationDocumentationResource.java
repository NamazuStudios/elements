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

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "The swagger definition in either JSON or YAML", hidden = true)
    public Swagger getJsonDocumentation(@PathParam("applicationNameOrId") final String applicationNameOrId) {
        return null;
    }

}
