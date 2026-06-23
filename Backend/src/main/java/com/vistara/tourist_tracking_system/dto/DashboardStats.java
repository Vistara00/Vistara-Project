package com.vistara.tourist_tracking_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private List<EmergencyAlertBreakdown> emergencyAlertBreakdown;
    private long activeVisitors;
    private long totalUsers;
    private long todayCheckins;
    private long todayCheckouts;
    private double dailyAverageRevenue;
    private List<DailyStat> attendanceDaily;
    private List<DailyStat> revenueDaily;      // optional: daily revenue for line/bar chart
    private List<WeeklyStat> attendanceWeekly;
}