package com.vistara.tourist_tracking_system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vistara.tourist_tracking_system.dto.MpesaStkRequest;
import com.vistara.tourist_tracking_system.dto.MpesaStkResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpesaService {

    @Value("${MPESA_BASE_URL}")
    private String baseUrl;

    @Value("${MPESA_CONSUMER_KEY}")
    private String consumerKey;

    @Value("${MPESA_CONSUMER_SECRET}")
    private String consumerSecret;

    @Value("${MPESA_SHORT_CODE}")
    private String shortCode;

    @Value("${MPESA_PASS_KEY}")
    private String passKey;

    @Value("${MPESA_STK_CALLBACK_URL}")
    private String stkCallbackUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getAccessToken() {
        String auth = consumerKey + ":" + consumerSecret;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.ISO_8859_1));
        String authHeader = "Basic " + new String(encodedAuth);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String tokenUrl = baseUrl + "/oauth/v1/generate?grant_type=client_credentials";
        ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.GET, entity, Map.class);

        if (response.getBody() != null && response.getBody().containsKey("access_token")) {
            return response.getBody().get("access_token").toString();
        }
        throw new RuntimeException("Failed to obtain M-Pesa access token");
    }

    public MpesaStkResponse stkPush(MpesaStkRequest request) {
        String accessToken = getAccessToken();
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
            return res;
        }
        throw new RuntimeException("Failed to initiate STK Push");
    }

    private String generatePassword(String timestamp) {
        String str = shortCode + passKey + timestamp;
        byte[] encoded = Base64.getEncoder().encode(str.getBytes(StandardCharsets.ISO_8859_1));
        return new String(encoded);
    }
}