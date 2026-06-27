package com.vistara.tourist_tracking_system.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MpesaCallbackPayload {
    @JsonProperty("Body")
    private Body body;

    @lombok.Data
    public static class Body {
        @JsonProperty("stkCallback")
        private StkCallback stkCallback;
    }

    @lombok.Data
    public static class StkCallback {
        @JsonProperty("MerchantRequestID")
        private String merchantRequestId;

        @JsonProperty("CheckoutRequestID")
        private String checkoutRequestId;

        @JsonProperty("ResultCode")
        private Integer resultCode;

        @JsonProperty("ResultDesc")
        private String resultDesc;

        @JsonProperty("CallbackMetadata")
        private CallbackMetadata callbackMetadata;
    }

    @lombok.Data
    public static class CallbackMetadata {
        @JsonProperty("Item")
        private List<Item> item;
    }

    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        // M-Pesa sends with capital letters: "Name", "Value"
        @JsonProperty("Name")
        private String name;

        @JsonProperty("Value")
        private Object value;

        // Also support lowercase if needed
        @JsonProperty("name")
        private String nameLower;

        @JsonProperty("value")
        private Object valueLower;
    }
}