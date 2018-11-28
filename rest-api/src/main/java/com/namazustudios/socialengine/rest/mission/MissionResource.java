//package com.namazustudios.socialengine.rest.mission;
//
//import com.namazustudios.socialengine.model.Pagination;
//import com.namazustudios.socialengine.service.mission.MissionService;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.Authorization;
//
//import javax.inject.Inject;
//import javax.ws.rs.DefaultValue;
//import javax.ws.rs.GET;
//import javax.ws.rs.POST;
//import javax.ws.rs.PUT;
//import javax.ws.rs.Path;
//import javax.ws.rs.PathParam;
//import javax.ws.rs.Produces;
//import javax.ws.rs.QueryParam;
//import javax.ws.rs.core.MediaType;
//
//import java.util.Set;
//
//import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;
//
//@Path("mission")
//@Api(value = "Missions",
//        description = "Manages missions, steps and rewards",
//        authorizations = {@Authorization(SESSION_SECRET)})
//@Produces(MediaType.APPLICATION_JSON)
//public class MissionResource {
//
//    private MissionService missionService;
//
//    @POST
//    @ApiOperation(value = "Creates a new digital Item",
//            notes = "Supplying an item object, this will create a new item with a newly assigned unique id.  " +
//                    "The Item representation returned in the response body is a representation of the Item as persisted " +
//                    "with a unique identifier signed and with its fields properly normalized.  The supplied item object " +
//                    "submitted with the request must have a name property that is unique across all items.")
//    public Item createItem(Item itemToBeCreated) {
//        return missionService.createItem(itemToBeCreated);
//    }
//
//
//    @GET
//    @ApiOperation(value = "Retrieves all Items",
//            notes = "Searches all items and returns all matching items, filtered by the passed in search parameters.  " +
//                    "If multiple tags are specified, then all items that contain at least one of the passed in tags is " +
//                    "returned.")
//    public Pagination<Item> getItems(@QueryParam("offset") @DefaultValue("0") final int offset,
//                                     @QueryParam("count") @DefaultValue("20") final int count,
//                                     @QueryParam("tags") final Set<String> tags,
//                                     @QueryParam("search") final String search) {
//        return missionService.getItems(offset, count, tags, search);
//    }
//
//    @GET
//    @Path("{identifier}")
//    @ApiOperation(value = "Retrieves a single Item by id or by name",
//            notes = "Looks up an item by the passed in identifier")
//    public Item getItemByIdentifier(@PathParam("identifier") String identifier) {
//        return missionService.getItemByIdOrName(identifier);
//    }
//
//    @PUT
//    @Path("{identifier}")
//    @ApiOperation(value = "Updates a single Item",
//            notes = "Supplying an item, this will update the Item identified by the identifier in the path with contents " +
//                    "from the passed in request body. ")
//    public Item updateItem(final Item updatedItem,
//                           @PathParam("identifier") String identifier) {
//        return missionService.updateItem(updatedItem);
//    }
//
//
//    public ItemService getItemService() {
//        return missionService;
//    }
//
//    @Inject
//    public void setItemService(ItemService itemService) {
//        this.missionService = itemService;
//    }
//}
