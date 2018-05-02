package com.namazustudios.socialengine.model.session;


import io.swagger.annotations.ApiModel;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@ApiModel(description = "Used to create a mock session with the server.")
public class MockSessionRequest {

    @Min(0)
    private Long expireUserAt;

    public Long getExpireUserAt() {
        return expireUserAt;
    }

    public void setExpireUserAt(Long expireUserAt) {
        this.expireUserAt = expireUserAt;
    }

}
