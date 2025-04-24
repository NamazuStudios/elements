package dev.getelements.elements.sdk.test.element.rs;

import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.ElementRegistrySupplier;
import dev.getelements.elements.sdk.service.user.UserService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

@Path("/message")
public class MessageEndpoint {

    private static final AtomicInteger counter = new AtomicInteger();

    private static final Map<Integer, Message> messages = new ConcurrentSkipListMap<>();

    private final UserService userService = ElementRegistrySupplier
            .getElementLocal(MessageEndpoint.class)
            .get()
            .find("dev.getelements.elements.sdk.service")
            .findAny()
            .get()
            .getServiceLocator()
            .getInstance(UserService.class);

    @POST
    public Response createMessage(final CreateMessageRequest createMessageRequest) {

        if (createMessageRequest.getMessage() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        final int id = counter.incrementAndGet();
        final long now = System.currentTimeMillis();

        final var message = new Message();
        message.setId(id);
        message.setUser(userService.getCurrentUser());
        message.setMessage(createMessageRequest.getMessage());
        message.setCreated(now);
        message.setUpdated(now);

        if (messages.putIfAbsent(id, message) != null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response
                .status(Response.Status.CREATED)
                .entity(message).build();

    }

    @PUT
    @Path("{messageId}")
    public Response updateMessage(
            @PathParam("messageId")
            final String messageId,
            final UpdateMessageRequest updateMessageRequest) {

        final int id;

        try {
            id = Integer.parseInt(messageId);
        } catch (NumberFormatException ex) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final var result = messages.computeIfPresent(id, (_id, existing) -> {
            final var updated = new Message();
            updated.setId(_id);
            updated.setCreated(existing.getCreated());
            updated.setUpdated(System.currentTimeMillis());
            updated.setMessage(updateMessageRequest.getMessage());
            return updated;
        });

        return result == null
                ? Response.status(Response.Status.NOT_FOUND).build()
                : Response.status(Response.Status.OK).entity(result).build();

    }

    @GET
    public Response getMessages() {
        return Response
                .status(Response.Status.OK)
                .entity(messages.values())
                .build();
    }

    @GET
    @Path("{messageId}")
    public Response getMessage(
            @PathParam("messageId")
            final String messageId) {

        final int id;

        try {
            id = Integer.parseInt(messageId);
        } catch (NumberFormatException ex) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final var message = messages.get(id);

        return message == null
                ? Response.status(Response.Status.NOT_FOUND).build()
                : Response.status(Response.Status.OK).entity(message).build();

    }

    @DELETE
    @Path("{messageId}")
    public Response deleteMessage(
            @PathParam("messageId")
            final String messageId) {

        final int id;

        try {
            id = Integer.parseInt(messageId);
        } catch (NumberFormatException ex) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final var removed = messages.remove(id);

        return removed == null
                ? Response.status(Response.Status.NOT_FOUND).build()
                : Response.status(Response.Status.NO_CONTENT).entity(removed).build();

    }

}
