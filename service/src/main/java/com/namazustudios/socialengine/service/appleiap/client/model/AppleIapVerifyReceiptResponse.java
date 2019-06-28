package com.namazustudios.socialengine.service.appleiap.client.model;

import com.namazustudios.socialengine.annotation.ClientSerializationStrategy;
import io.swagger.annotations.ApiModel;

import java.util.Objects;

import static com.namazustudios.socialengine.annotation.ClientSerializationStrategy.SNAKE;

@ApiModel
@ClientSerializationStrategy(SNAKE)
public class AppleIapVerifyReceiptResponse {
    private Integer status;

    private AppleIapGrandUnifiedReceipt receipt;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public AppleIapGrandUnifiedReceipt getReceipt() {
        return receipt;
    }

    public void setReceipt(AppleIapGrandUnifiedReceipt receipt) {
        this.receipt = receipt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppleIapVerifyReceiptResponse that = (AppleIapVerifyReceiptResponse) o;
        return Objects.equals(getStatus(), that.getStatus()) &&
                Objects.equals(getReceipt(), that.getReceipt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStatus(), getReceipt());
    }

    @Override
    public String toString() {
        return "AppleIapVerifyReceiptResponse{" +
                "status=" + status +
                ", receipt=" + receipt +
                '}';
    }
}