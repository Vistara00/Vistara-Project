package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.exception.DuplicateResourceException;
import com.vistara.tourist_tracking_system.model.PasswordResetToken;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.repository.PasswordResetTokenRepository;
import com.vistara.tourist_tracking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    // Token valid for 15 minutes
    private static final int TOKEN_EXPIRY_MINUTES = 15;

    @Transactional
    public String generateResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DuplicateResourceException("No account found with that email"));

        // Invalidate any existing tokens for this user
        tokenRepository.invalidateAllTokensForUser(user);

        // Generate a new secure token
        String rawToken = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(rawToken);
        resetToken.setUser(user);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));
        resetToken.setUsed(false);

        tokenRepository.save(resetToken);

        // In production: send this token via email using JavaMailSender.
        // For now we log it — replace this with your email service call.
        log.info("Password reset token for {}: {}", email, rawToken);

        return rawToken;
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new DuplicateResourceException("Invalid or expired reset token"));

        if (resetToken.isUsed()) {
            throw new DuplicateResourceException("Reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new DuplicateResourceException("Reset token has expired — please request a new one");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used so it cannot be replayed
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}