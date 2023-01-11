package com.namazustudios.socialengine.dao.mongo.model.blockchain;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;

import java.util.Objects;

@Embedded
public class MongoSmartContractAddress {

    @Property
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoSmartContractAddress that = (MongoSmartContractAddress) o;
        return Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }

    @Override
    public String toString() {
        return "MongoSmartContractAddress{" +
                "address='" + address + '\'' +
                '}';
    }

}
