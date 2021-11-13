package com.namazustudios.socialengine.rt.lua.guice.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
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
    public SimpleModel getModel(@PathParam("id") final String id) {
        final SimpleModel model = models.get(id);
        if (model == null) throw new NotFoundException();
        return model;
    }

    @POST
    public SimpleModel create(final SimpleModel simpleModel) {
        final String id = randomUUID().toString();
        simpleModel.setId(id);
        models.put(id, simpleModel);
        return simpleModel;
    }

    @PUT
    @Path("{id}")
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
