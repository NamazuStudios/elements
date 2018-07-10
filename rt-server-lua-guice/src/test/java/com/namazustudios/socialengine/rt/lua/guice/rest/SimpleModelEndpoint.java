package com.namazustudios.socialengine.rt.lua.guice.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static java.util.UUID.randomUUID;

@Path("simple")
@Produces(MediaType.APPLICATION_JSON)
public class SimpleModelEndpoint {

    private final ConcurrentMap<String, SimpleModel> models = new ConcurrentSkipListMap<>();

    @GET
    public List<SimpleModel> getModels() {
        return new ArrayList<>(models.values());
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
