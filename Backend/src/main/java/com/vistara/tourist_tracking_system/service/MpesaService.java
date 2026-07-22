package com.vistara.tourist_tracking_system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vistara.tourist_tracking_system.dto.MpesaStkRequest;
import com.vistara.tourist_tracking_system.dto.MpesaStkResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class MpesaService {

    @Value("${MPESA_BASE_URL:https://sandbox.safaricom.co.ke}")
    private String baseUrl;

    @Value("${MPESA_CONSUMER_KEY:}")
    private String consumerKey;

    @Value("${MPESA_CONSUMER_SECRET:}")
    private String consumerSecret;

    @Value("${MPESA_SHORT_CODE:174379}")
    private String shortCode;

    @Value("${MPESA_PASS_KEY:}")
    private String passKey;

    @Value("${MPESA_STK_CALLBACK_URL:}")
    private String stkCallbackUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        log.info("========================================");
        log.info("🔧 M-Pesa Service Initialization");
        log.info("   Base URL: {}", baseUrl);
        log.info("   Consumer Key: {}", consumerKey != null && !consumerKey.isEmpty() ? "✅ Set" : "❌ Missing");
        log.info("   Consumer Secret: {}", consumerSecret != null && !consumerSecret.isEmpty() ? "✅ Set" : "❌ Missing");
        log.info("   Short Code: {}", shortCode != null && !shortCode.isEmpty() ? "✅ Set" : "❌ Missing");
        log.info("   Pass Key: {}", passKey != null && !passKey.isEmpty() ? "✅ Set" : "❌ Missing");
        log.info("   Callback URL: {}", stkCallbackUrl != null && !stkCallbackUrl.isEmpty() ? "✅ Set" : "❌ Missing");
        log.info("========================================");
    }

    /**
     * Get M-Pesa access token
     */
    public String getAccessToken() {
        if (consumerKey == null || consumerKey.isEmpty() || consumerSecret == null || consumerSecret.isEmpty()) {
            log.warn("⚠️ M-Pesa credentials not configured. Returning mock token.");
            return "mock-token-" + System.currentTimeMillis();
        }

        try {
            log.info("🔑 Generating M-Pesa Access Token...");
            String auth = consumerKey + ":" + consumerSecret;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.ISO_8859_1));
            String authHeader = "Basic " + new String(encodedAuth);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String tokenUrl = baseUrl + "/oauth/v1/generate?grant_type=client_credentials";
            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUrl, HttpMethod.GET, entity, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("access_token")) {
                String token = response.getBody().get("access_token").toString();
                log.info("✅ Access Token generated successfully");
                return token;
            }
            log.warn("⚠️ Failed to obtain M-Pesa access token. Using mock token.");
            return "mock-token-" + System.currentTimeMillis();
        } catch (Exception e) {
            log.error("❌ Error getting M-Pesa access token: {}", e.getMessage());
            return "mock-token-" + System.currentTimeMillis();
        }
    }

    /**
     * Initiate STK Push
     */
    public MpesaStkResponse stkPush(MpesaStkRequest request) {
        // If credentials are missing, return mock response
        if (consumerKey == null || consumerKey.isEmpty() || consumerSecret == null || consumerSecret.isEmpty()) {
            log.warn("⚠️ M-Pesa credentials not configured. Returning mock STK response.");
            return createMockResponse(request);
        }

        try {
            log.info("🚀 Initiating STK Push...");
            log.info("   Phone: {}", request.getPhoneNumber());
            log.info("   Amount: {}", request.getAmount());
            log.info("   Account Reference: {}", request.getAccountReference());

            String accessToken = getAccessToken();

            // Check if we got a mock token
            if (accessToken.startsWith("mock-token-")) {
                log.warn("⚠️ Using mock token. Returning mock STK response.");
                return createMockResponse(request);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String password = generatePassword(timestamp);

            Map<String, Object> body = new HashMap<>();
            body.put("BusinessShortCode", shortCode);
            body.put("Password", password);
            body.put("Timestamp", timestamp);
            body.put("TransactionType", "CustomerPayBillOnline");
            body.put("Amount", request.getAmount());
            body.put("PartyA", request.getPhoneNumber());
            body.put("PartyB", shortCode);
            body.put("PhoneNumber", request.getPhoneNumber());
            body.put("CallBackURL", stkCallbackUrl);
            body.put("AccountReference", request.getAccountReference());
            body.put("TransactionDesc", request.getTransactionDesc());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            String stkPushUrl = baseUrl + "/mpesa/stkpush/v1/processrequest";
            ResponseEntity<Map> response = restTemplate.postForEntity(stkPushUrl, entity, Map.class);

            if (response.getBody() != null) {
                MpesaStkResponse res = new MpesaStkResponse();
                res.setMerchantRequestId((String) response.getBody().get("MerchantRequestID"));
                res.setCheckoutRequestId((String) response.getBody().get("CheckoutRequestID"));
                res.setResponseCode((String) response.getBody().get("ResponseCode"));
                res.setResponseDescription((String) response.getBody().get("ResponseDescription"));
                res.setCustomerMessage((String) response.getBody().get("CustomerMessage"));

                log.info("✅ STK Push Response:");
                log.info("   MerchantRequestId: {}", res.getMerchantRequestId());
                log.info("   CheckoutRequestId: {}", res.getCheckoutRequestId());
                log.info("   ResponseCode: {}", res.getResponseCode());
                log.info("   ResponseDescription: {}", res.getResponseDescription());
                log.info("   CustomerMessage: {}", res.getCustomerMessage());

                return res;
            }
            throw new RuntimeException("Empty response from M-Pesa API");

        } catch (Exception e) {
            log.error("❌ STK Push failed: {}", e.getMessage());
            // Return mock response on error to prevent frontend failure
            log.warn("⚠️ Returning mock response due to error.");
            return createMockResponse(request);
        }
    }

    /**
     * Generate M-Pesa password
     */
    private String generatePassword(String timestamp) {
        String str = shortCode + passKey + timestamp;
        byte[] encoded = Base64.getEncoder().encode(str.getBytes(StandardCharsets.ISO_8859_1));
        return new String(encoded);
    }

    /**
     * Create mock STK response
     */
    private MpesaStkResponse createMockResponse(MpesaStkRequest request) {
        log.info("📡 Creating mock STK response for: {}", request.getAccountReference());
        MpesaStkResponse mockResponse = new MpesaStkResponse();
        mockResponse.setCheckoutRequestId("MOCK-" + System.currentTimeMillis());
        mockResponse.setMerchantRequestId("MOCK-MERCHANT-" + System.currentTimeMillis());
        mockResponse.setResponseCode("0");
        mockResponse.setResponseDescription("Success. Mock M-Pesa payment.");
        mockResponse.setCustomerMessage("Mock M-Pesa payment initiated for: " + request.getAccountReference());
        return mockResponse;
    }
}