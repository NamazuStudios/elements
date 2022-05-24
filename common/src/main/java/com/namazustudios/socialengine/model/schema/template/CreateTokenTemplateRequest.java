package com.namazustudios.socialengine.model.schema.template;

import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.blockchain.ElementsSmartContract;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;
import java.util.Map;

@ApiModel(description = "Represents a request to create a MetadataSpec definition.")
public class CreateTokenTemplateRequest {

    @ApiModelProperty("The name of the template.")
    private String name;

    @ApiModelProperty("The display name of the template.")
    private String displayName;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The Metadata Id that the template reference to.")
    String metadataSpecId;

    @ApiModelProperty("The Metadata of the template.")
    Map<String, Object>  metadata;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The Contract Id that the template reference to.")
    String contractId;

    @ApiModelProperty("The User Id of the owner of the template.")
    String userId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMetadataSpecId() {
        return metadataSpecId;
    }

    public void setMetadataSpecId(String metadataSpecId) {
        this.metadataSpecId = metadataSpecId;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
