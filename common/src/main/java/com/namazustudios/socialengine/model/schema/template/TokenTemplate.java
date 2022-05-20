package com.namazustudios.socialengine.model.schema.template;

import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.blockchain.ElementsSmartContract;
import com.namazustudios.socialengine.model.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;
import java.util.Map;

@ApiModel
public class TokenTemplate {

    @NotNull(groups = ValidationGroups.Update.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @ApiModelProperty("The unique ID of the template itself.")
    private String id;

    @ApiModelProperty("The Name of the template.")
    private String name;

    @ApiModelProperty("The Display Name of the template.")
    private String displayName;

    @ApiModelProperty("The owner of the template.")
    private User user;

    @ApiModelProperty("The Metadata Spec for the token template")
    private MetadataSpec metadataSpec;

    @ApiModelProperty("The Metadata for the token template")
    Map<String, Object> metadata;

    @ApiModelProperty("The Smart Contract the template reference to")
    private ElementsSmartContract contract;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public MetadataSpec getMetadataSpec() {
        return metadataSpec;
    }

    public void setMetadataSpec(MetadataSpec metadataSpec) {
        this.metadataSpec = metadataSpec;
    }

    public ElementsSmartContract getContract() {
        return contract;
    }

    public void setContract(ElementsSmartContract contract) {
        this.contract = contract;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
