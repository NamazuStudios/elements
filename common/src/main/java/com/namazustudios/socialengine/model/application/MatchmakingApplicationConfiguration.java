package com.namazustudios.socialengine.model.application;

import com.namazustudios.socialengine.model.match.MatchingAlgorithm;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@ApiModel(
    value = "Matchmaking Applicaiont Configuration",
    description = "This configures the matchmaking system.  More specifically, this configures which scripts and" +
                  "methods will be called when a successful match has been made.")
public class MatchmakingApplicationConfiguration {

    @NotNull
    @ApiModelProperty("Specifies the matching algorithm to use.")
    private MatchingAlgorithm algorithm;

    @NotNull
    @ApiModelProperty("Specifies the callback to execute when a successful match has been made.")
    private CallbackDefinition success;

    public MatchingAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(MatchingAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public CallbackDefinition getSuccess() {
        return success;
    }

    public void setSuccess(CallbackDefinition success) {
        this.success = success;
    }

}
