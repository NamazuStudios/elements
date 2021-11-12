package com.namazustudios.socialengine.model.auth;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;

@ApiModel
public class AuthScheme {

    @NotNull(groups = ValidationGroups.Update.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @ApiModelProperty("The unique ID of the auth scheme.")
    public String id;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @ApiModelProperty("A unique name used to identify the scheme within the instance of Elements.")
    public String aud;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @ApiModelProperty("A base-64 encoded public key.")
    public String pubKey;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @ApiModelProperty("The highest permitted user level this particular scheme will authorize.")
    public String userLevel;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @ApiModelProperty("A list of issuers allowed to use this scheme.")
    public List<String> allowedIssuers;
}
