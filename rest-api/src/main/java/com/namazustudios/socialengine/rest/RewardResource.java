package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.mission.Reward;
import com.namazustudios.socialengine.service.reward.RewardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;

@Path("reward")
@Api(value = "Rewards",
        description = "Manages rewards",
        authorizations = {@Authorization(SESSION_SECRET)})
@Produces(MediaType.APPLICATION_JSON)
public class RewardResource {

    private RewardService rewardService;

    @POST
    @ApiOperation(value = "Creates a new reward",
            notes = "Supplying the item db ref, quantity to be given upon giving the reward, and optional metadata, " +
                    "this will create a new reward to be redeemed via a RewardIssuance.")
    public Reward createReward(Reward reward) {
        return getRewardService().createReward(reward);
    }

    @GET
    @Path("{rewardId}")
    @ApiOperation(value = "Retrieves a single Reward by id.",
            notes = "Looks up a Reward by the provided identifier")
    public Reward getReward(@PathParam("rewardId") String rewardId) {
        return getRewardService().getReward(rewardId);
    }

    @DELETE
    @Path("{rewardId}")
    @ApiOperation(value = "Deletes the Reward identified by id",
            notes = "Deletes a reward by the provided identifier")
    public void deleteReward(@PathParam("rewardId") String rewardId) {
        getRewardService().delete(rewardId);
    }


    public RewardService getRewardService() {
        return rewardService;
    }

    @Inject
    public void setRewardService(RewardService rewardService) {
        this.rewardService = rewardService;
    }
}
