package com.vistara.tourist_tracking_system.repository;

import com.vistara.tourist_tracking_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNationalId(String nationalId);

    List<User> findAllByOrderByIdAsc();

    List<User> findByRole(User.Role role);

    List<User> findByRoleAndActiveTrue(User.Role role);

    // ✅ CORRECTED: Use alertStatus instead of status
    @Query("SELECT u FROM User u WHERE u.role = 'RANGER' AND u.active = true AND u NOT IN " +
            "(SELECT DISTINCT e.assignedRanger FROM EmergencyAlert e WHERE e.alertStatus = 'RESPONDING')")
    List<User> findAvailableRangers();

    // Alternative method using the correct field name
    @Query("SELECT u FROM User u WHERE u.role = 'RANGER' AND u.active = true AND u.id NOT IN " +
            "(SELECT DISTINCT e.assignedRanger.id FROM EmergencyAlert e WHERE e.alertStatus = 'RESPONDING')")
    List<User> findAvailableRangersAlt();
}