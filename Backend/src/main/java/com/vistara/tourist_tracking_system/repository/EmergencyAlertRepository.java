package com.vistara.tourist_tracking_system.repository;

import com.vistara.tourist_tracking_system.model.EmergencyAlert;
import com.vistara.tourist_tracking_system.model.EmergencyAlert.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmergencyAlertRepository extends JpaRepository<EmergencyAlert, Long> {

    List<EmergencyAlert> findByStatus(AlertStatus status);

    List<EmergencyAlert> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<EmergencyAlert> findByStatusOrderByTimestampDesc(AlertStatus status);
}