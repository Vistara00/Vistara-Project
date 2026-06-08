package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.exception.DuplicateResourceException;
import com.vistara.tourist_tracking_system.model.PasswordResetToken;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.repository.PasswordResetTokenRepository;
import com.vistara.tourist_tracking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.from-name}")
    private String fromName;

    private static final int TOKEN_EXPIRY_MINUTES = 15;

    @Transactional
    public String generateResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DuplicateResourceException(
                        "No account found with that email"));

        // Invalidate all existing unused tokens for this user
        tokenRepository.invalidateAllTokensForUser(user);

        // Generate a new secure UUID token
        String rawToken = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(rawToken);
        resetToken.setUser(user);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));
        resetToken.setUsed(false);
        tokenRepository.save(resetToken);

        // Send the token via email
        sendResetEmail(user, rawToken);

        // Still return the raw token so the controller can include it
        // in the response for Postman testing.
        // REMOVE this return value (return null or void) once you go to production.
        return rawToken;
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new DuplicateResourceException(
                        "Invalid or expired reset token"));

        if (resetToken.isUsed()) {
            throw new DuplicateResourceException(
                    "Reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new DuplicateResourceException(
                    "Reset token has expired — please request a new one");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used so it cannot be replayed
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        // Notify the user their password was changed
        sendPasswordChangedEmail(user);
    }

    // -------------------------------------------------------
    // Private mail helpers
    // -------------------------------------------------------

    private void sendResetEmail(User user, String rawToken) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromAddress);
            mail.setTo(user.getEmail());
            mail.setSubject("Vistara — Password Reset Request");
            mail.setText(
                    "Hello " + user.getFullName() + ",\n\n" +
                            "You requested a password reset for your Vistara account.\n\n" +
                            "Your reset token is:\n\n" +
                            "    " + rawToken + "\n\n" +
                            "Submit this token to:\n" +
                            "POST /api/v1/auth/reset-password\n\n" +
                            "{\n" +
                            "    \"token\": \"" + rawToken + "\",\n" +
                            "    \"newPassword\": \"yourNewPassword\"\n" +
                            "}\n\n" +
                            "This token expires in " + TOKEN_EXPIRY_MINUTES + " minutes.\n\n" +
                            "If you did not request this, please ignore this email — " +
                            "your password will not be changed.\n\n" +
                            "— The Vistara Team"
            );
            mailSender.send(mail);
            log.info("Password reset email sent to {}", user.getEmail());
        } catch (Exception e) {
            // Log the failure but don't surface it to the caller —
            // the token was saved successfully; a mail outage shouldn't
            // block the reset flow. The token is also returned in the
            // API response for testing purposes.
            log.error("Failed to send reset email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private void sendPasswordChangedEmail(User user) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromAddress);
            mail.setTo(user.getEmail());
            mail.setSubject("Vistara — Your Password Has Been Changed");
            mail.setText(
                    "Hello " + user.getFullName() + ",\n\n" +
                            "Your Vistara account password was successfully changed.\n\n" +
                            "If you did not make this change, please contact support immediately " +
                            "or request another password reset.\n\n" +
                            "— The Vistara Team"
            );
            mailSender.send(mail);
            log.info("Password changed confirmation email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password changed email to {}: {}",
                    user.getEmail(), e.getMessage());
        }
    }
}