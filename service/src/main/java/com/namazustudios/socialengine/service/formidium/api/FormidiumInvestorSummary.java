package com.namazustudios.socialengine.service.formidium.api;

import java.io.Serializable;
import java.util.Objects;

public class FormidiumInvestorSummary implements Serializable {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormidiumInvestorSummary that = (FormidiumInvestorSummary) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FormidiumInvestorSummary{");
        sb.append("id='").append(id).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
