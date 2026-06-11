package com.vistara.tourist_tracking_system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
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
    public static class Item {
        private String name;
        private Object value;
    }
}