package dev.getelements.elements.model.blockchain.bsc;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

@ApiModel
@Deprecated
public class Web3jWallet {

    @ApiModelProperty("The IV for encrypting this wallet.")
    private String iv;

    @ApiModelProperty("The IV for encrypting this wallet.")
    private String salt;

    @ApiModelProperty("The name given to this wallet.")
    private String name;

    @ApiModelProperty("The version of this wallet.")
    private String version;

    @ApiModelProperty("The accounts associated with this wallet.")
    private List<String> accounts;

    @ApiModelProperty(value = "The accounts associated with this wallet.")
    private List<String> addresses;

    @ApiModelProperty("The extra object data associated with this wallet.")
    private Object extra;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public List<String> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<String> accounts) {
        this.accounts = accounts;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Web3jWallet that = (Web3jWallet) o;
        return Objects.equals(getIv(), that.getIv()) && Objects.equals(getSalt(), that.getSalt()) && Objects.equals(getName(), that.getName()) && Objects.equals(getVersion(), that.getVersion()) && Objects.equals(getAccounts(), that.getAccounts()) && Objects.equals(getAddresses(), that.getAddresses()) && Objects.equals(getExtra(), that.getExtra());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIv(), getSalt(), getName(), getVersion(), getAccounts(), getAddresses(), getExtra());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Web3jWallet{");
        sb.append("iv='").append(iv).append('\'');
        sb.append(", salt='").append(salt).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", accounts=").append(accounts);
        sb.append(", addresses=").append(addresses);
        sb.append(", extra=").append(extra);
        sb.append('}');
        return sb.toString();
    }

}
