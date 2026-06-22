package com.vistara.tourist_tracking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyAlertBreakdown {
    private String status;
    private String priority;
    private long count;
}