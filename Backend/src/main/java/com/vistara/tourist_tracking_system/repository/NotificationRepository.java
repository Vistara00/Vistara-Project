package com.vistara.tourist_tracking_system.repository;

import com.vistara.tourist_tracking_system.model.Notification;
import com.vistara.tourist_tracking_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndReadFalseOrderByCreatedAtDesc(User user);

    long countByUserAndReadFalse(User user);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.user = :user")
    void markAllAsRead(@Param("user") User user);

    @Query("SELECT n FROM Notification n WHERE n.broadcast = true ORDER BY n.createdAt DESC")
    List<Notification> findBroadcastNotifications();

    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.type = :type AND n.referenceId = :referenceId")
    List<Notification> findByUserAndTypeAndReferenceId(@Param("user") User user,
                                                       @Param("type") String type,
                                                       @Param("referenceId") Long referenceId);
}