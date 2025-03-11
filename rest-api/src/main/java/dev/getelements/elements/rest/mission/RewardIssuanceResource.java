package dev.getelements.elements.rest.mission;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.reward.RewardIssuance.State;
import dev.getelements.elements.sdk.model.reward.RewardIssuanceRedemptionResult;

import dev.getelements.elements.sdk.service.rewardissuance.RewardIssuanceService;
import io.swagger.v3.oas.annotations.Operation;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("reward_issuance")
@Produces(MediaType.APPLICATION_JSON)
public class RewardIssuanceResource {

    private RewardIssuanceService rewardIssuanceService;

    @GET
    @Path("{rewardIssuanceId}")
    @Operation(summary = "Retrieves a single RewardIssuance by id.")
    public RewardIssuance getRewardIssuance(@PathParam("rewardIssuanceId") final String rewardIssuanceId) {
        return getRewardIssuanceService().getRewardIssuance(rewardIssuanceId);
    }

    @GET
    @Operation(summary = "Retrieves the current user's reward issuances, optionally filtered by the given state.")
    public Pagination<RewardIssuance> getRewardIssuances(
            @QueryParam("offset") @DefaultValue("0")  final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("states") final List<State> states,
            @QueryParam("tags") final List<String> tags) {
        return getRewardIssuanceService().getRewardIssuances(offset, count, states, tags);
    }

    @PUT
    @Path("{rewardIssuanceId}/redeem")
    @Operation(summary = "Redeems the RewardIssuance.")
    public RewardIssuanceRedemptionResult redeemRewardIssuance(@PathParam("rewardIssuanceId") String rewardIssuanceId) {
        return getRewardIssuanceService().redeemRewardIssuance(rewardIssuanceId);
    }

    @PUT
    @Path("redeem")
    @Operation(summary = "Redeems the given list of RewardIssuances.")
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
