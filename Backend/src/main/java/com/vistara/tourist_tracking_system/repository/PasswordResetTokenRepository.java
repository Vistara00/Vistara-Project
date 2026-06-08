package com.vistara.tourist_tracking_system.repository;

import com.vistara.tourist_tracking_system.model.PasswordResetToken;
import com.vistara.tourist_tracking_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    // FIX: updated JPQL field name to "tokenUsed" to match the renamed
    // field in PasswordResetToken entity
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.tokenUsed = true " +
            "WHERE t.user = :user AND t.tokenUsed = false")
    void invalidateAllTokensForUser(@Param("user") User user);
}