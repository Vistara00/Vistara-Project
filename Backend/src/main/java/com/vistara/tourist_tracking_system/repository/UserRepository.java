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

    // Add this method
    List<User> findByRoleAndActive(User.Role role, boolean active);

    // Get active users by role
    List<User> findByRoleAndActiveTrue(User.Role role);

    // Get inactive users by role
    List<User> findByRoleAndActiveFalse(User.Role role);

    // Find available rangers (not assigned to any active alert)
    @Query("SELECT u FROM User u WHERE u.role = 'RANGER' AND u.active = true AND u.id NOT IN " +
            "(SELECT DISTINCT e.assignedRanger.id FROM EmergencyAlert e WHERE e.alertStatus = 'RESPONDING')")
    List<User> findAvailableRangers();

    // Count users by role
    long countByRole(User.Role role);

    // Count active users by role
    long countByRoleAndActiveTrue(User.Role role);

    // Find users by role with pagination (if needed)
    List<User> findByRoleOrderByFullNameAsc(User.Role role);

    // Search users by name or email
    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(String searchTerm);
}