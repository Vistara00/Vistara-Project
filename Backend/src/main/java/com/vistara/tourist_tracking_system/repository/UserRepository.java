package com.vistara.tourist_tracking_system.repository;

import com.vistara.tourist_tracking_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository  // ✅ Make sure this annotation is present
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailIgnoreCase(@Param("email") String email);

    boolean existsByEmail(String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    boolean existsByNationalId(String nationalId);

    List<User> findAllByOrderByIdAsc();

    List<User> findByRole(User.Role role);

    List<User> findByRoleAndActive(User.Role role, boolean active);

    List<User> findByRoleAndActiveTrue(User.Role role);

    List<User> findByRoleAndActiveFalse(User.Role role);

    @Query("SELECT u FROM User u WHERE u.role = 'PARK_RANGER' AND u.active = true AND u.id NOT IN " +
            "(SELECT DISTINCT e.assignedRanger.id FROM EmergencyAlert e WHERE e.alertStatus = 'RESPONDING')")
    List<User> findAvailableRangers();

    long countByRole(User.Role role);

    long countByRoleAndActiveTrue(User.Role role);

    List<User> findByRoleOrderByFullNameAsc(User.Role role);

    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);
}