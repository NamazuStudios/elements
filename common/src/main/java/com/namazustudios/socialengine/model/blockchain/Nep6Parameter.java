package com.namazustudios.socialengine.model.blockchain;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

public class Nep6Parameter {

    @ApiModelProperty("parameter name")
    private String paramName;

    @ApiModelProperty("parameter type")
    private ContractParameterType paramType;

    public Nep6Parameter(String paramName, ContractParameterType paramType) {
        this.paramName = paramName;
        this.paramType = paramType;
    }

    public Nep6Parameter() {
    }

    public String getParamName() {
        return paramName;
    }

    public ContractParameterType getParamType() {
        return paramType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nep6Parameter nep6Parameter = (Nep6Parameter) o;
        return Objects.equals(paramName, nep6Parameter.paramName) &&
                paramType == nep6Parameter.paramType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(paramName, paramType);
    }

    @Override
    public String toString() {
        return "Nep6Parameter{" +
                "paramName='" + paramName + '\'' +
                ", paramType=" + paramType +
                '}';
    }
}
