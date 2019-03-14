package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.reward.RewardIssuance;
import com.namazustudios.socialengine.model.reward.RewardIssuance.State;
import com.namazustudios.socialengine.model.reward.RewardIssuanceRedemptionResult;
import com.namazustudios.socialengine.service.rewardissuance.RewardIssuanceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.util.List;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;

@Path("reward_issuance")
@Api(value = "Reward Issuances",
        description = "Manages reward issuances",
        authorizations = {@Authorization(SESSION_SECRET)})
@Produces(MediaType.APPLICATION_JSON)
public class RewardIssuanceResource {

    private RewardIssuanceService rewardIssuanceService;

    @GET
    @Path("{rewardIssuanceId}")
    @ApiOperation(value = "Retrieves a single RewardIssuance by id.")
    public RewardIssuance getRewardIssuance(@PathParam("rewardIssuanceId") final String rewardIssuanceId) {
        return getRewardIssuanceService().getRewardIssuance(rewardIssuanceId);
    }

    @GET
    @ApiOperation(value = "Retrieves the current user's reward issuances, optionally filtered by the given state.")
    public Pagination<RewardIssuance> getRewardIssuances(
            @QueryParam("offset") @DefaultValue("0")  final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("states") final List<State> states,
            @QueryParam("tags") final List<String> tags) {
        return getRewardIssuanceService().getRewardIssuances(offset, count, states, tags);
    }

    @PUT
    @Path("{rewardIssuanceId}/redeem")
    @ApiOperation(value = "Redeems the RewardIssuance.")
    public RewardIssuanceRedemptionResult redeemRewardIssuance(@PathParam("rewardIssuanceId") String rewardIssuanceId) {
        return getRewardIssuanceService().redeemRewardIssuance(rewardIssuanceId);
    }

    @PUT
    @Path("redeem")
    @ApiOperation(value = "Redeems the given list of RewardIssuances.")
    public List<RewardIssuanceRedemptionResult> redeemRewardIssuances(final List<String> rewardIssuanceIds) {
        return getRewardIssuanceService().redeemRewardIssuances(rewardIssuanceIds);
    }

    public RewardIssuanceService getRewardIssuanceService() {
        return rewardIssuanceService;
    }

    @Inject
    public void setRewardIssuanceService(RewardIssuanceService rewardIssuanceService) {
        this.rewardIssuanceService = rewardIssuanceService;
    }
}
