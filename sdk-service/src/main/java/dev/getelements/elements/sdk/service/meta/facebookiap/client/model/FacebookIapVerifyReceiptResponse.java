package dev.getelements.elements.sdk.service.meta.facebookiap.client.model;

import dev.getelements.elements.rt.annotation.ClientSerializationStrategy;

import java.util.List;
import java.util.Objects;

import static dev.getelements.elements.rt.annotation.ClientSerializationStrategy.META_GRAPH;

@ClientSerializationStrategy(META_GRAPH)
public class FacebookIapVerifyReceiptResponse {

    private String id;

    private FacebookRef user;

    private FacebookRef application;

    private List<FacebookPaymentItem> items;

    private String amount;

    private String currency;

    private String status;

    private String createdTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FacebookRef getUser() {
        return user;
    }

    public void setUser(FacebookRef user) {
        this.user = user;
    }

    public FacebookRef getApplication() {
        return application;
    }

    public void setApplication(FacebookRef application) {
        this.application = application;
    }

    public List<FacebookPaymentItem> getItems() {
        return items;
    }

    public void setItems(List<FacebookPaymentItem> items) {
        this.items = items;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FacebookIapVerifyReceiptResponse that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(user, that.user) && Objects.equals(application, that.application) && Objects.equals(items, that.items) && Objects.equals(amount, that.amount) && Objects.equals(currency, that.currency) && Objects.equals(status, that.status) && Objects.equals(createdTime, that.createdTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, application, items, amount, currency, status, createdTime);
    }

    @Override
    public String toString() {
        return "FacebookIapVerifyReceiptResponse{" +
                "id='" + id + '\'' +
                ", user=" + user +
                ", application=" + application +
                ", items=" + items +
                ", amount='" + amount + '\'' +
                ", currency='" + currency + '\'' +
                ", status='" + status + '\'' +
                ", createdTime='" + createdTime + '\'' +
                '}';
    }
}