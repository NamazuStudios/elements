package com.namazustudios.socialengine.model.savedata;

import io.swagger.annotations.ApiModel;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@ApiModel(description = "Updates a save data document. This accepts the contents of the document as well as the " +
                        "versioning information required to take the update properly.")
public class UpdateSaveDataDocumentRequest {

    private Boolean force;

    private String version;

    @NotNull
    private String contents;

    public Boolean getForce() {
        return force;
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateSaveDataDocumentRequest that = (UpdateSaveDataDocumentRequest) o;
        return Objects.equals(getForce(), that.getForce()) && Objects.equals(getVersion(), that.getVersion()) && Objects.equals(getContents(), that.getContents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getForce(), getVersion(), getContents());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UpdateSaveDataDocument{");
        sb.append("force=").append(force);
        sb.append(", version='").append(version).append('\'');
        sb.append(", contents='").append(contents).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
