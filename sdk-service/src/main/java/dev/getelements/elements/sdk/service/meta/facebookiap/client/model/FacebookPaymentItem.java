package dev.getelements.elements.sdk.service.meta.facebookiap.client.model;

import java.util.Objects;

public class FacebookPaymentItem {

    private String product;   // URL or product id configured in Facebook

    private int quantity; // The number of units purchased

    private String amount; // The amount of currency

    private String currency; // The type of currency

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FacebookPaymentItem that)) return false;
        return quantity == that.quantity && Objects.equals(product, that.product) && Objects.equals(amount, that.amount) && Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, quantity, amount, currency);
    }

    @Override
    public String toString() {
        return "FacebookPaymentItem{" +
                "product='" + product + '\'' +
                ", quantity=" + quantity +
                ", amount='" + amount + '\'' +
                ", currency='" + currency + '\'' +
                '}';
    }
}
