package com.vistara.tourist_tracking_system.dto;

import lombok.Data;

@Data
public class AdminCheckInRequest {
    private Long bookingId;        // use either bookingId or walkInUserId
    private Long walkInUserId;
    private String vehicleRegistrationOverride;
    private String notes;
}