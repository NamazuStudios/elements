package dev.getelements.elements.model.formidium;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

@ApiModel
public class CreateFormidiumInvestorRequest implements Serializable {

    @ApiModelProperty("The Elements User ID")
    private String userId;

    @NotNull
    @ApiModelProperty("The Formidium API Parameters. (See Formidium Documentation).")
    private Map<String, Object> formidiumApiParameters;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, Object> getFormidiumApiParameters() {
        return formidiumApiParameters;
    }

    public void setFormidiumApiParameters(Map<String, Object> formidiumApiParameters) {
        this.formidiumApiParameters = formidiumApiParameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateFormidiumInvestorRequest that = (CreateFormidiumInvestorRequest) o;
        return Objects.equals(getUserId(), that.getUserId()) && Objects.equals(getFormidiumApiParameters(), that.getFormidiumApiParameters());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getFormidiumApiParameters());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateFormidiumInvestorRequest{");
        sb.append("userId='").append(userId).append('\'');
        sb.append(", formidiumApiParameters=").append(formidiumApiParameters);
        sb.append('}');
        return sb.toString();
    }

}
