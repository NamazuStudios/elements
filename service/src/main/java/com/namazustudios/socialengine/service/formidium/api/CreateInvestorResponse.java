package com.namazustudios.socialengine.service.formidium.api;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class CreateInvestorResponse extends FormidiumApiResponse implements Serializable {

    private FormidiumInvestorSummary data;

    public FormidiumInvestorSummary getData() {
        return data;
    }

    public void setData(FormidiumInvestorSummary data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CreateInvestorResponse that = (CreateInvestorResponse) o;
        return Objects.equals(getData(), that.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getData());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateInvestorResponse{");
        sb.append("data=").append(data);
        sb.append('}');
        return sb.toString();
    }

}
