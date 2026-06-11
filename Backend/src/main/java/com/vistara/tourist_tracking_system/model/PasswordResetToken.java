package com.vistara.tourist_tracking_system.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", length = 6, nullable = false, unique = true)
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // FIX: renamed from "used" to "tokenUsed" to avoid Lombok generating
    // isUsed() conflicting with the custom isExpired() method naming pattern.
    // Also prevents Hibernate misreading the boolean field name.
    @Column(name = "used")
    private boolean tokenUsed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // Explicit accessor to avoid Lombok boolean naming confusion
    public boolean isUsed() {
        return tokenUsed;
    }

    public void setUsed(boolean used) {
        this.tokenUsed = used;
    }
}