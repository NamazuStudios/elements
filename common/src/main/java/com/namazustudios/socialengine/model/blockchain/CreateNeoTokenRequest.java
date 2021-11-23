package com.namazustudios.socialengine.model.blockchain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@ApiModel(description = "Represents a request to create a NeoToken definition.")
public class CreateNeoTokenRequest {

    @NotNull
    @ApiModelProperty("The token definition to deploy.")
    private Token token;

    @ApiModelProperty("Any meta data for this token.")
    private Map<String, Object> metaData;

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Map<String, Object> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, Object> metaData) {
        this.metaData = metaData;
    }
}
