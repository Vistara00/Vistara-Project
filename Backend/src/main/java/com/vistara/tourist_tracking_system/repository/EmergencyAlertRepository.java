package com.vistara.tourist_tracking_system.repository;

import com.vistara.tourist_tracking_system.model.EmergencyAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmergencyAlertRepository extends JpaRepository<EmergencyAlert, Long> {

    // Use the fully qualified inner enum type to avoid Spring Data ambiguity
    List<EmergencyAlert> findByAlertStatus(EmergencyAlert.AlertStatus alertStatus);

    List<EmergencyAlert> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<EmergencyAlert> findByAlertStatusOrderByCreatedAtDesc(EmergencyAlert.AlertStatus alertStatus);
}