package com.namazustudios.socialengine.model.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

import static com.namazustudios.socialengine.Constants.Regexp.NO_WHITE_SPACE;

@ApiModel(description = "Defines a script module to invoke.")
public class EventDefinition implements Serializable {

    @NotNull
    @Pattern(regexp = NO_WHITE_SPACE)
    @ApiModelProperty("Specifies the module to invoke.")
    private String module;

    @NotNull
    @ApiModelProperty("The array of arguments to pass to the invoked methods.")
    private String[] args;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

}
