package dev.getelements.elements.rest.application;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.net.URI;

import static dev.getelements.elements.Constants.DOC_OUTSIDE_URL;
import static dev.getelements.elements.util.URIs.appendPath;
import static javax.ws.rs.core.Response.Status.MOVED_PERMANENTLY;

/**
 * Created by patricktwohig on 8/23/17.
 */
@Api(hidden = true)
@Path("application/{applicationNameOrId}/swagger.json")
public class ApplicationDocumentationResource {

    private URI docOutsideUrl;

    @GET
    @ApiOperation(code = 302, value = "Redirects to documentation microservice.", hidden = true)
    public Response getJsonDocumentation(@PathParam("applicationNameOrId") final String applicationNameOrId) {

        final var location = appendPath(
            getDocOutsideUrl(),
            "rest",
            "swagger",
            "2",
            applicationNameOrId,
            "swagger.json"
        );

        return Response
                .status(MOVED_PERMANENTLY.getStatusCode())
                .location(location)
            .build();

    }

    public URI getDocOutsideUrl() {
        return docOutsideUrl;
    }

    @Inject
    public void setDocOutsideUrl(@Named(DOC_OUTSIDE_URL) final URI docOutsideUrl) {
        this.docOutsideUrl = docOutsideUrl;
    }

}
