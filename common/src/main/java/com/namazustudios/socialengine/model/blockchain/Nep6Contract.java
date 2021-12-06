package com.namazustudios.socialengine.model.blockchain;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

public class Nep6Contract {

    @ApiModelProperty("script")
    private String script;

    @ApiModelProperty("paramenters")
    private List<Nep6Parameter> nep6Parameters;

    @ApiModelProperty("deployed")
    private Boolean deployed;

    public Nep6Contract() {
    }

    public Nep6Contract(String script, List<Nep6Parameter> nep6Parameters, Boolean deployed) {
        this.script = script;
        this.nep6Parameters = nep6Parameters;
        this.deployed = deployed;
    }

    public String getScript() {
        return script;
    }

    public List<Nep6Parameter> getParameters() {
        return nep6Parameters;
    }

    public Boolean getDeployed() {
        return deployed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nep6Contract)) return false;
        Nep6Contract contract = (Nep6Contract) o;
        return Objects.equals(getScript(), contract.getScript()) &&
                Objects.equals(getParameters(), contract.getParameters()) &&
                Objects.equals(getDeployed(), contract.getDeployed());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getScript(), getParameters(), getDeployed());
    }

    @Override
    public String toString() {
        return "Contract{" +
                "script='" + script + '\'' +
                ", nep6Parameters=" + nep6Parameters +
                ", deployed=" + deployed +
                '}';
    }
}
