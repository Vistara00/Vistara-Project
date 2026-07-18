package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.MpesaStkRequest;
import com.vistara.tourist_tracking_system.dto.MpesaStkResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "mpesa.enabled", havingValue = "false", matchIfMissing = true)
public class MockMpesaService {

    public String getAccessToken() {
        log.info("Mock M-Pesa: Returning mock access token");
        return "mock-token-" + System.currentTimeMillis();
    }

    public MpesaStkResponse stkPush(MpesaStkRequest request) {
        log.info("Mock M-Pesa: STK Push for booking {}", request.getAccountReference());

        MpesaStkResponse response = new MpesaStkResponse();
        response.setCheckoutRequestId("MOCK-" + System.currentTimeMillis());
        response.setMerchantRequestId("MOCK-MERCHANT-" + System.currentTimeMillis());
        response.setResponseCode("0");
        response.setResponseDescription("Success. Mock M-Pesa payment.");
        response.setCustomerMessage("Mock M-Pesa payment initiated for booking: " + request.getAccountReference());

        return response;
    }
}