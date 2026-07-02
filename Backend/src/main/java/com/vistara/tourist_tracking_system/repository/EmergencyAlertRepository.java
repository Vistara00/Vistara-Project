package com.vistara.tourist_tracking_system.repository;

import com.vistara.tourist_tracking_system.model.EmergencyAlert;
import com.vistara.tourist_tracking_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyAlertRepository extends JpaRepository<EmergencyAlert, Long> {

    List<EmergencyAlert> findByAlertStatus(EmergencyAlert.AlertStatus status);

    List<EmergencyAlert> findByAlertStatusOrderByCreatedAtDesc(EmergencyAlert.AlertStatus status);

    List<EmergencyAlert> findByAlertStatusIn(List<EmergencyAlert.AlertStatus> statuses);

    List<EmergencyAlert> findAllByOrderByCreatedAtDesc();

    List<EmergencyAlert> findByAssignedRangerOrderByCreatedAtDesc(User ranger);

    List<EmergencyAlert> findByAssignedRangerAndAlertStatus(User ranger, EmergencyAlert.AlertStatus status);

    List<EmergencyAlert> findByAssignedRangerId(Long rangerId);

    boolean existsByAssignedRangerIdAndAlertStatus(Long rangerId, EmergencyAlert.AlertStatus status);

    @Query("SELECT e.alertStatus, e.priority, COUNT(e) FROM EmergencyAlert e GROUP BY e.alertStatus, e.priority")
    List<Object[]> countByStatusAndPriority();

    long countByAlertStatus(EmergencyAlert.AlertStatus status);
}