package com.vistara.tourist_tracking_system.dto;

import lombok.Data;

@Data
public class MpesaStkResponse {
    private String merchantRequestId;
    private String checkoutRequestId;
    private String responseCode;
    private String responseDescription;
    private String customerMessage;
}