package com.namazustudios.socialengine.model.blockchain.bsc;

import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class Nep6Account {

    @ApiModelProperty("address")
    private String address;

    @ApiModelProperty("label")
    private String label;

    @ApiModelProperty("isDefault")
    private Boolean isDefault;

    @ApiModelProperty("isLocked")
    private Boolean isLocked;

    @ApiModelProperty("key")
    private String key;

    @ApiModelProperty("contract")
    private Nep6Contract contract;

    @ApiModelProperty("extra")
    private Object extra;

    public Nep6Account() {
    }

    public Nep6Account(String address, String label, Boolean isDefault, Boolean isLocked, String key,
                       Nep6Contract contract, Object extra) {
        this.address = address;
        this.label = label;
        this.isDefault = isDefault;
        this.isLocked = isLocked;
        this.key = key;
        this.contract = contract;
        this.extra = extra;
    }

    public String getAddress() {
        return address;
    }

    public String getLabel() {
        return label;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public Boolean getIsLocked() {
        return isLocked;
    }

    public String getKey() {
        return key;
    }

    public Nep6Contract getContract() {
        return contract;
    }

    public Object getExtra() {
        return extra;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nep6Account)) return false;
        Nep6Account account = (Nep6Account) o;
        return Objects.equals(getAddress(), account.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress(), getLabel(), getIsDefault(), getIsLocked(), getKey(),
                getContract(), getExtra());
    }

    @Override
    public String toString() {
        return "Account{" +
                "address='" + address + '\'' +
                ", label='" + label + '\'' +
                ", isDefault=" + isDefault +
                ", isLocked=" + isLocked +
                ", key='" + key + '\'' +
                ", contract=" + contract +
                ", extra=" + extra +
                '}';
    }
}
