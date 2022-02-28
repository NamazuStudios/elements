package com.namazustudios.socialengine.model.blockchain.neo;

import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Nep6Wallet {

    @ApiModelProperty("The name given to this wallet.")
    private String name;

    @ApiModelProperty("The version of this wallet.")
    private String version;

    @ApiModelProperty("The scrypt of this wallet.")
    private ScryptParams scrypt;

    @ApiModelProperty("The accounts associated with this wallet.")
    private List<Nep6Account> accounts;

    @ApiModelProperty("The extra object data associated with this wallet.")
    private Object extra;

    public Nep6Wallet() {
    }

    public Nep6Wallet(String name, String version, ScryptParams scrypt, List<Nep6Account> accounts, Object extra) {
        this.name = name;
        this.version = version;
        this.scrypt = scrypt;
        this.accounts = (accounts == null) ? new ArrayList<Nep6Account>() :  accounts;
        this.extra = extra;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public ScryptParams getScrypt() {
        return scrypt;
    }

    public List<Nep6Account> getAccounts() {
        return accounts;
    }

    public Object getExtra() {
        return extra;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nep6Wallet)) return false;
        Nep6Wallet that = (Nep6Wallet) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getVersion(), that.getVersion()) &&
                Objects.equals(getScrypt(), that.getScrypt()) &&
                Objects.equals(getAccounts(), that.getAccounts()) &&
                Objects.equals(getExtra(), that.getExtra());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getVersion(), getScrypt(), getAccounts(), getExtra());
    }

    @Override
    public String toString() {
        return "Nep6Wallet{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", scrypt=" + scrypt +
                ", accounts=" + accounts +
                ", extra=" + extra +
                '}';
    }
}
