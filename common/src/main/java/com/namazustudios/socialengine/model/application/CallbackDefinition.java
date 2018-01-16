package com.namazustudios.socialengine.model.application;

import com.namazustudios.socialengine.model.match.MatchingAlgorithm;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@ApiModel(
    value = "Defines a Simple Script Callback.",
    description = "Defines a method and a module to invoke.")
public class CallbackDefinition implements Serializable {

    @NotNull
    @Pattern(regexp = "[\\w]+[\\w\\.]*")
    @ApiModelProperty("Specifies the module to invoke.")
    private String module;

    @NotNull
    @Pattern(regexp = "[A-Za-z]+[A-Za-z0-9]*")
    @ApiModelProperty("Specifies the method to invoke.")
    private String method;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

}
