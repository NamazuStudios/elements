package com.namazustudios.socialengine.model.appleiapreceipt;

import io.swagger.annotations.ApiModel;

import java.util.*;

@ApiModel
public class AppleIapGrandUnifiedReceipt {
    private String bundleId;

    private String applicationVersion;

    private String originalApplicationVersion;

    private String appItemId;

    private String versionExternalIdentifier;

    private Date receiptCreationDate;

    private Date requestDate;

    private Date originalPurchaseDate;

    private List<AppleIapGrandUnifiedReceiptPurchase> inApp;

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public String getOriginalApplicationVersion() {
        return originalApplicationVersion;
    }

    public void setOriginalApplicationVersion(String originalApplicationVersion) {
        this.originalApplicationVersion = originalApplicationVersion;
    }

    public String getAppItemId() {
        return appItemId;
    }

    public void setAppItemId(String appItemId) {
        this.appItemId = appItemId;
    }

    public String getVersionExternalIdentifier() {
        return versionExternalIdentifier;
    }

    public void setVersionExternalIdentifier(String versionExternalIdentifier) {
        this.versionExternalIdentifier = versionExternalIdentifier;
    }

    public Date getReceiptCreationDate() {
        return receiptCreationDate;
    }

    public void setReceiptCreationDate(Date receiptCreationDate) {
        this.receiptCreationDate = receiptCreationDate;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public Date getOriginalPurchaseDate() {
        return originalPurchaseDate;
    }

    public void setOriginalPurchaseDate(Date originalPurchaseDate) {
        this.originalPurchaseDate = originalPurchaseDate;
    }

    public List<AppleIapGrandUnifiedReceiptPurchase> getInApp() {
        return inApp;
    }

    public void setInApp(List<AppleIapGrandUnifiedReceiptPurchase> inApp) {
        this.inApp = inApp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppleIapGrandUnifiedReceipt that = (AppleIapGrandUnifiedReceipt) o;
        return Objects.equals(getBundleId(), that.getBundleId()) &&
                Objects.equals(getApplicationVersion(), that.getApplicationVersion()) &&
                Objects.equals(getOriginalApplicationVersion(), that.getOriginalApplicationVersion()) &&
                Objects.equals(getAppItemId(), that.getAppItemId()) &&
                Objects.equals(getVersionExternalIdentifier(), that.getVersionExternalIdentifier()) &&
                Objects.equals(getReceiptCreationDate(), that.getReceiptCreationDate()) &&
                Objects.equals(getRequestDate(), that.getRequestDate()) &&
                Objects.equals(getOriginalPurchaseDate(), that.getOriginalPurchaseDate()) &&
                Objects.equals(getInApp(), that.getInApp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBundleId(), getApplicationVersion(), getOriginalApplicationVersion(), getAppItemId(), getVersionExternalIdentifier(), getReceiptCreationDate(), getRequestDate(), getOriginalPurchaseDate(), getInApp());
    }

    @Override
    public String toString() {
        return "AppleIapGrandUnifiedReceipt{" +
                "bundleId='" + bundleId + '\'' +
                ", applicationVersion='" + applicationVersion + '\'' +
                ", originalApplicationVersion='" + originalApplicationVersion + '\'' +
                ", appItemId='" + appItemId + '\'' +
                ", versionExternalIdentifier='" + versionExternalIdentifier + '\'' +
                ", receiptCreationDate=" + receiptCreationDate +
                ", requestDate=" + requestDate +
                ", originalPurchaseDate=" + originalPurchaseDate +
                ", inApp=" + inApp +
                '}';
    }
}