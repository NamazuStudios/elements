package ${package}.rest

import ${package}.HelloWorldApplication
import ${package}.model.ExamplePostRequest
import ${package}.model.ExamplePostResponse
import ${package}.model.ExamplePutRequest
import ${package}.model.ExamplePutResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType

@Tag(name = HelloWorldApplication.OPENAPI_TAG)
@Path("/examplecontent")
class ExampleContent {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Example POST request", description = "Example produces/consumes for POST")
    fun examplePost(examplePostRequest: ExamplePostRequest): ExamplePostResponse {

        // Normally we'd create a new object in the database with a POST request, but for demonstration
        // purposes, we'll just return an example response object
        val response = ExamplePostResponse()
        response.name = examplePostRequest.name
        return response
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Example PUT request", description = "Example produces/consumes for PUT")
    fun examplePost(examplePutRequest: ExamplePutRequest): ExamplePutResponse {

        // Normally we'd overwrite an existing object in the database with a PUT request, but for demonstration
        // purposes, we'll just return an example response object
        val response = ExamplePutResponse()
        response.name = examplePutRequest.name
        return response
    }

    @PUT
    @Path("{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Example PUT request with a path param", description = "Example produces/consumes for PUT with a path param")
    fun examplePutWithPathParam(@PathParam("name") name: String, examplePutRequest: ExamplePutRequest): ExamplePutResponse {

        // Normally we'd overwrite an existing object in the database with a "name" property that matches the "name" path
        // param with this PUT request, but for demonstration purposes, we'll just return an example response object
        val response = ExamplePutResponse()
        response.name = examplePutRequest.name
        response.metadata = mapOf("name" to name)
        return response
    }

}