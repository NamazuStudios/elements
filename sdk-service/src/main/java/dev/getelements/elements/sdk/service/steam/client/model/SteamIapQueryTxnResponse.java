package dev.getelements.elements.sdk.service.steam.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents the response from the Steam ISteamMicroTxn/QueryTxn/v3 API endpoint.
 *
 * @see <a href="https://partner.steamgames.com/doc/webapi/ISteamMicroTxn#QueryTxn">Steam ISteamMicroTxn/QueryTxn</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamIapQueryTxnResponse {

    @JsonProperty("response")
    private Response response;

    /**
     * Returns the response body from Steam.
     *
     * @return the response
     */
    public Response getResponse() {
        return response;
    }

    /**
     * Sets the response body from Steam.
     *
     * @param response the response
     */
    public void setResponse(Response response) {
        this.response = response;
    }

    /**
     * Returns true if the Steam API reported a successful result.
     *
     * @return true if result is "OK"
     */
    public boolean isOk() {
        return response != null && response.isOk();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {

        @JsonProperty("result")
        private String result;

        @JsonProperty("params")
        private Params params;

        @JsonProperty("lineitems")
        private LineItems lineItems;

        /**
         * Returns the Steam result code ("OK" on success).
         *
         * @return the result
         */
        public String getResult() {
            return result;
        }

        /**
         * Sets the Steam result code.
         *
         * @param result the result
         */
        public void setResult(String result) {
            this.result = result;
        }

        /**
         * Returns the transaction parameters.
         *
         * @return the params
         */
        public Params getParams() {
            return params;
        }

        /**
         * Sets the transaction parameters.
         *
         * @param params the params
         */
        public void setParams(Params params) {
            this.params = params;
        }

        /**
         * Returns the line items for this transaction.
         *
         * @return the line items
         */
        public LineItems getLineItems() {
            return lineItems;
        }

        /**
         * Sets the line items for this transaction.
         *
         * @param lineItems the line items
         */
        public void setLineItems(LineItems lineItems) {
            this.lineItems = lineItems;
        }

        /**
         * Returns true if the Steam API result is "OK".
         *
         * @return true if successful
         */
        public boolean isOk() {
            return "OK".equals(result);
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Params {

        @JsonProperty("orderid")
        private String orderId;

        @JsonProperty("transid")
        private String transactionId;

        @JsonProperty("steamid")
        private String steamId;

        @JsonProperty("status")
        private String status;

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("time")
        private String time;

        @JsonProperty("country")
        private String country;

        @JsonProperty("price")
        private String price;

        @JsonProperty("vat")
        private String vat;

        /**
         * Returns the Steam order ID.
         *
         * @return the order ID
         */
        public String getOrderId() {
            return orderId;
        }

        /**
         * Sets the Steam order ID.
         *
         * @param orderId the order ID
         */
        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        /**
         * Returns the Steam internal transaction ID.
         *
         * @return the transaction ID
         */
        public String getTransactionId() {
            return transactionId;
        }

        /**
         * Sets the Steam internal transaction ID.
         *
         * @param transactionId the transaction ID
         */
        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        /**
         * Returns the Steam user ID (64-bit SteamID).
         *
         * @return the Steam ID
         */
        public String getSteamId() {
            return steamId;
        }

        /**
         * Sets the Steam user ID.
         *
         * @param steamId the Steam ID
         */
        public void setSteamId(String steamId) {
            this.steamId = steamId;
        }

        /**
         * Returns the transaction status (e.g. Committed, Approved, Refunded).
         *
         * @return the status
         */
        public String getStatus() {
            return status;
        }

        /**
         * Sets the transaction status.
         *
         * @param status the status
         */
        public void setStatus(String status) {
            this.status = status;
        }

        /**
         * Returns the ISO 4217 currency code.
         *
         * @return the currency
         */
        public String getCurrency() {
            return currency;
        }

        /**
         * Sets the ISO 4217 currency code.
         *
         * @param currency the currency
         */
        public void setCurrency(String currency) {
            this.currency = currency;
        }

        /**
         * Returns the transaction time string from Steam.
         *
         * @return the time
         */
        public String getTime() {
            return time;
        }

        /**
         * Sets the transaction time string.
         *
         * @param time the time
         */
        public void setTime(String time) {
            this.time = time;
        }

        /**
         * Returns the country code for the transaction.
         *
         * @return the country
         */
        public String getCountry() {
            return country;
        }

        /**
         * Sets the country code.
         *
         * @param country the country
         */
        public void setCountry(String country) {
            this.country = country;
        }

        /**
         * Returns the price in the smallest currency unit (e.g. cents).
         *
         * @return the price
         */
        public String getPrice() {
            return price;
        }

        /**
         * Sets the price.
         *
         * @param price the price
         */
        public void setPrice(String price) {
            this.price = price;
        }

        /**
         * Returns the VAT amount.
         *
         * @return the vat
         */
        public String getVat() {
            return vat;
        }

        /**
         * Sets the VAT amount.
         *
         * @param vat the vat
         */
        public void setVat(String vat) {
            this.vat = vat;
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LineItems {

        @JsonProperty("lineitem")
        private List<LineItem> lineItemList;

        /**
         * Returns the list of line items in this transaction.
         *
         * @return the line item list
         */
        public List<LineItem> getLineItemList() {
            return lineItemList;
        }

        /**
         * Sets the list of line items.
         *
         * @param lineItemList the line item list
         */
        public void setLineItemList(List<LineItem> lineItemList) {
            this.lineItemList = lineItemList;
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LineItem {

        @JsonProperty("itemid")
        private String itemId;

        @JsonProperty("qty")
        private String quantity;

        @JsonProperty("amount")
        private String amount;

        @JsonProperty("vat")
        private String vat;

        @JsonProperty("itemstatus")
        private String itemStatus;

        @JsonProperty("description")
        private String description;

        @JsonProperty("category")
        private String category;

        /**
         * Returns the item ID for this line item.
         *
         * @return the item ID
         */
        public String getItemId() {
            return itemId;
        }

        /**
         * Sets the item ID.
         *
         * @param itemId the item ID
         */
        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        /**
         * Returns the quantity purchased.
         *
         * @return the quantity
         */
        public String getQuantity() {
            return quantity;
        }

        /**
         * Sets the quantity.
         *
         * @param quantity the quantity
         */
        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }

        /**
         * Returns the line item amount in the smallest currency unit.
         *
         * @return the amount
         */
        public String getAmount() {
            return amount;
        }

        /**
         * Sets the amount.
         *
         * @param amount the amount
         */
        public void setAmount(String amount) {
            this.amount = amount;
        }

        /**
         * Returns the VAT amount for this line item.
         *
         * @return the vat
         */
        public String getVat() {
            return vat;
        }

        /**
         * Sets the VAT amount.
         *
         * @param vat the vat
         */
        public void setVat(String vat) {
            this.vat = vat;
        }

        /**
         * Returns the item status (e.g. Succeeded).
         *
         * @return the item status
         */
        public String getItemStatus() {
            return itemStatus;
        }

        /**
         * Sets the item status.
         *
         * @param itemStatus the item status
         */
        public void setItemStatus(String itemStatus) {
            this.itemStatus = itemStatus;
        }

        /**
         * Returns the description of the item.
         *
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Sets the description.
         *
         * @param description the description
         */
        public void setDescription(String description) {
            this.description = description;
        }

        /**
         * Returns the category of the item.
         *
         * @return the category
         */
        public String getCategory() {
            return category;
        }

        /**
         * Sets the category.
         *
         * @param category the category
         */
        public void setCategory(String category) {
            this.category = category;
        }

    }

}
