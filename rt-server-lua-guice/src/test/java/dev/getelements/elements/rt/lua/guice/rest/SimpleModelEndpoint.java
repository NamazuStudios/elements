package dev.getelements.elements.rt.lua.guice.rest;

import org.glassfish.jersey.media.multipart.MultiPart;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

@Path("simple")
@Produces(MediaType.APPLICATION_JSON)
public class SimpleModelEndpoint {

    private static final ConcurrentMap<String, SimpleModel> models = new ConcurrentSkipListMap<>();

    public static void clear() {
        models.clear();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<SimpleModel> getModels(
            @QueryParam("hello") final String hello,
            @QueryParam("world") final String world) {
        return hello == null && world == null ? new ArrayList<>(models.values()) : models
            .values()
            .stream()
            .filter(m -> Objects.equals(m.getHello(), hello) && Objects.equals(m.getWorld(), world))
            .collect(toList());
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public SimpleModel getModel(@PathParam("id") final String id) {
        final SimpleModel model = models.get(id);
        if (model == null) throw new NotFoundException();
        return model;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SimpleModel create(final SimpleModel simpleModel) {
        final String id = randomUUID().toString();
        simpleModel.setId(id);
        models.put(id, simpleModel);
        return simpleModel;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public SimpleModel createMultipartForm(final MultiPart multiPart) {

        final var hello = getFormPart(multiPart, "hello");
        final var world = getFormPart(multiPart, "world");

        final var simpleModel = new SimpleModel();
        simpleModel.setHello(hello);
        simpleModel.setWorld(world);

        return create(simpleModel);

    }

    private String getFormPart(final MultiPart multiPart, final String name) {
        return multiPart.getBodyParts()
            .stream()
            .filter(bp -> bp.getContentDisposition().getParameters().get("name").equals(name))
            .map(bp -> bp.getEntityAs(String.class))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SimpleModel update(@PathParam("id") final String id, final SimpleModel replacement) {

        replacement.setId(id);

        SimpleModel existing;

        do {
            existing = models.get(id);
            if (existing == null) throw new NotFoundException();
        } while (!models.replace(id, existing, replacement));

        return replacement;

    }

    @DELETE
    @Path("{id}")
    public void delete(@PathParam("id") final String id) {
        if (models.remove(id) == null) {
            throw new NotFoundException();
        }
    }

}
