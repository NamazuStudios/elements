package com.namazustudios.socialengine.service.appleiap.client.model;

import io.swagger.annotations.ApiModel;

import java.util.*;

@ApiModel
public class AppleIapGrandUnifiedReceiptPurchase {
    private Integer quantity;

    private String productId;

    private String transactionId;

    private String originalTransactionId;

    private Date purchaseDate;

    private Date originalPurchaseDate;

    private Date expiresDate;

    private Integer expirationIntent;

    private Boolean isInBillingRetryPeriod;

    private Boolean isTrialPeriod;

    private Boolean isInIntroOfferPeriod;

    private Date cancellationDate;

    private Integer cancellationReason;

    private String appItemId;

    private String versionExternalIdentifier;

    private String webOrderLineItemId;

    private Boolean autoRenewStatus;

    private String autoRenewProductId;

    private Boolean priceConsentStatus;

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getOriginalTransactionId() {
        return originalTransactionId;
    }

    public void setOriginalTransactionId(String originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Date getOriginalPurchaseDate() {
        return originalPurchaseDate;
    }

    public void setOriginalPurchaseDate(Date originalPurchaseDate) {
        this.originalPurchaseDate = originalPurchaseDate;
    }

    public Date getExpiresDate() {
        return expiresDate;
    }

    public void setExpiresDate(Date expiresDate) {
        this.expiresDate = expiresDate;
    }

    public Integer getExpirationIntent() {
        return expirationIntent;
    }

    public void setExpirationIntent(Integer expirationIntent) {
        this.expirationIntent = expirationIntent;
    }

    public Boolean getInBillingRetryPeriod() {
        return isInBillingRetryPeriod;
    }

    public void setInBillingRetryPeriod(Boolean inBillingRetryPeriod) {
        isInBillingRetryPeriod = inBillingRetryPeriod;
    }

    public Boolean getTrialPeriod() {
        return isTrialPeriod;
    }

    public void setTrialPeriod(Boolean trialPeriod) {
        isTrialPeriod = trialPeriod;
    }

    public Boolean getInIntroOfferPeriod() {
        return isInIntroOfferPeriod;
    }

    public void setInIntroOfferPeriod(Boolean inIntroOfferPeriod) {
        isInIntroOfferPeriod = inIntroOfferPeriod;
    }

    public Date getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(Date cancellationDate) {
        this.cancellationDate = cancellationDate;
    }

    public Integer getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(Integer cancellationReason) {
        this.cancellationReason = cancellationReason;
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

    public String getWebOrderLineItemId() {
        return webOrderLineItemId;
    }

    public void setWebOrderLineItemId(String webOrderLineItemId) {
        this.webOrderLineItemId = webOrderLineItemId;
    }

    public Boolean getAutoRenewStatus() {
        return autoRenewStatus;
    }

    public void setAutoRenewStatus(Boolean autoRenewStatus) {
        this.autoRenewStatus = autoRenewStatus;
    }

    public String getAutoRenewProductId() {
        return autoRenewProductId;
    }

    public void setAutoRenewProductId(String autoRenewProductId) {
        this.autoRenewProductId = autoRenewProductId;
    }

    public Boolean getPriceConsentStatus() {
        return priceConsentStatus;
    }

    public void setPriceConsentStatus(Boolean priceConsentStatus) {
        this.priceConsentStatus = priceConsentStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppleIapGrandUnifiedReceiptPurchase that = (AppleIapGrandUnifiedReceiptPurchase) o;
        return Objects.equals(getQuantity(), that.getQuantity()) &&
                Objects.equals(getProductId(), that.getProductId()) &&
                Objects.equals(getTransactionId(), that.getTransactionId()) &&
                Objects.equals(getOriginalTransactionId(), that.getOriginalTransactionId()) &&
                Objects.equals(getPurchaseDate(), that.getPurchaseDate()) &&
                Objects.equals(getOriginalPurchaseDate(), that.getOriginalPurchaseDate()) &&
                Objects.equals(getExpiresDate(), that.getExpiresDate()) &&
                Objects.equals(getExpirationIntent(), that.getExpirationIntent()) &&
                Objects.equals(isInBillingRetryPeriod, that.isInBillingRetryPeriod) &&
                Objects.equals(isTrialPeriod, that.isTrialPeriod) &&
                Objects.equals(isInIntroOfferPeriod, that.isInIntroOfferPeriod) &&
                Objects.equals(getCancellationDate(), that.getCancellationDate()) &&
                Objects.equals(getCancellationReason(), that.getCancellationReason()) &&
                Objects.equals(getAppItemId(), that.getAppItemId()) &&
                Objects.equals(getVersionExternalIdentifier(), that.getVersionExternalIdentifier()) &&
                Objects.equals(getWebOrderLineItemId(), that.getWebOrderLineItemId()) &&
                Objects.equals(getAutoRenewStatus(), that.getAutoRenewStatus()) &&
                Objects.equals(getAutoRenewProductId(), that.getAutoRenewProductId()) &&
                Objects.equals(getPriceConsentStatus(), that.getPriceConsentStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getQuantity(), getProductId(), getTransactionId(), getOriginalTransactionId(), getPurchaseDate(), getOriginalPurchaseDate(), getExpiresDate(), getExpirationIntent(), isInBillingRetryPeriod, isTrialPeriod, isInIntroOfferPeriod, getCancellationDate(), getCancellationReason(), getAppItemId(), getVersionExternalIdentifier(), getWebOrderLineItemId(), getAutoRenewStatus(), getAutoRenewProductId(), getPriceConsentStatus());
    }

    @Override
    public String toString() {
        return "AppleIapGrandUnifiedReceiptPurchase{" +
                "quantity=" + quantity +
                ", productId='" + productId + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", originalTransactionId='" + originalTransactionId + '\'' +
                ", purchaseDate=" + purchaseDate +
                ", originalPurchaseDate=" + originalPurchaseDate +
                ", expiresDate=" + expiresDate +
                ", expirationIntent=" + expirationIntent +
                ", isInBillingRetryPeriod=" + isInBillingRetryPeriod +
                ", isTrialPeriod=" + isTrialPeriod +
                ", isInIntroOfferPeriod=" + isInIntroOfferPeriod +
                ", cancellationDate=" + cancellationDate +
                ", cancellationReason=" + cancellationReason +
                ", appItemId='" + appItemId + '\'' +
                ", versionExternalIdentifier='" + versionExternalIdentifier + '\'' +
                ", webOrderLineItemId='" + webOrderLineItemId + '\'' +
                ", autoRenewStatus=" + autoRenewStatus +
                ", autoRenewProductId='" + autoRenewProductId + '\'' +
                ", priceConsentStatus=" + priceConsentStatus +
                '}';
    }
}