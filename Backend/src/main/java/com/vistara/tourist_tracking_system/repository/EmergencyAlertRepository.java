package com.vistara.tourist_tracking_system.repository;

import com.vistara.tourist_tracking_system.model.EmergencyAlert;
import com.vistara.tourist_tracking_system.model.User;  // ← ADD THIS IMPORT
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmergencyAlertRepository extends JpaRepository<EmergencyAlert, Long> {

    // Use the fully qualified inner enum type to avoid Spring Data ambiguity
    List<EmergencyAlert> findByAlertStatus(EmergencyAlert.AlertStatus alertStatus);

    List<EmergencyAlert> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<EmergencyAlert> findByAlertStatusOrderByCreatedAtDesc(EmergencyAlert.AlertStatus alertStatus);

    // Get all alerts ordered by creation date (descending)
    List<EmergencyAlert> findAllByOrderByCreatedAtDesc();

    // ✅ Get alerts assigned to a specific ranger, ordered by creation date (newest first)
    List<EmergencyAlert> findByAssignedRangerOrderByCreatedAtDesc(User assignedRanger);

    // ✅ Get alerts assigned to a ranger with specific status
    List<EmergencyAlert> findByAssignedRangerAndAlertStatus(User assignedRanger, EmergencyAlert.AlertStatus status);

    // Group by status and priority
    @Query("SELECT ea.alertStatus, ea.priority, COUNT(ea) FROM EmergencyAlert ea GROUP BY ea.alertStatus, ea.priority")
    List<Object[]> countByStatusAndPriority();
}