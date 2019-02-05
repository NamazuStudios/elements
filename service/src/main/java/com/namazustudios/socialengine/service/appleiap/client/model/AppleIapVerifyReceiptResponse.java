package com.namazustudios.socialengine.service.appleiap.client.model;

import io.swagger.annotations.ApiModel;

import java.util.*;

@ApiModel
public class AppleIapVerifyReceiptResponse {
    private Integer status;

    private AppleIapGrandUnifiedReceipt appleIapGrandUnifiedReceipt;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public AppleIapGrandUnifiedReceipt getAppleIapGrandUnifiedReceipt() {
        return appleIapGrandUnifiedReceipt;
    }

    public void setAppleIapGrandUnifiedReceipt(AppleIapGrandUnifiedReceipt appleIapGrandUnifiedReceipt) {
        this.appleIapGrandUnifiedReceipt = appleIapGrandUnifiedReceipt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppleIapVerifyReceiptResponse that = (AppleIapVerifyReceiptResponse) o;
        return Objects.equals(getStatus(), that.getStatus()) &&
                Objects.equals(getAppleIapGrandUnifiedReceipt(), that.getAppleIapGrandUnifiedReceipt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStatus(), getAppleIapGrandUnifiedReceipt());
    }

    @Override
    public String toString() {
        return "AppleIapReceiptResponse{" +
                "status=" + status +
                ", appleIapGrandUnifiedReceipt=" + appleIapGrandUnifiedReceipt +
                '}';
    }
}