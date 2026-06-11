package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.exception.DuplicateResourceException;
import com.vistara.tourist_tracking_system.model.PasswordResetToken;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.repository.PasswordResetTokenRepository;
import com.vistara.tourist_tracking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.time.LocalDateTime;

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
    private static final int TOKEN_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public String generateResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new DuplicateResourceException("No account found with that email"));

        // Invalidate all existing unused tokens for this user
        tokenRepository.invalidateAllTokensForUser(user);

        // Generate a 6-digit numeric token
        String rawToken = generateNumericToken();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(rawToken);
        resetToken.setUser(user);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));
        resetToken.setUsed(false);
        tokenRepository.save(resetToken);

        // Send the token via HTML email
        sendResetEmailHtml(user, rawToken);

        // Return token for testing (remove in production)
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

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        sendPasswordChangedEmailHtml(user);
    }

    // -------------------------------------------------------
    // Helper: generate a 6-digit token (100000–999999)
    // -------------------------------------------------------
    private String generateNumericToken() {
        return String.format("%06d", random.nextInt(900000) + 100000);
    }

    // -------------------------------------------------------
    // HTML email for reset token
    // -------------------------------------------------------
    private void sendResetEmailHtml(User user, String token) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(user.getEmail());
            helper.setSubject("🔐 Reset Your Vistara Password");

            String htmlContent = buildResetEmailHtml(user.getFullName(), token);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Password reset email (HTML) sent to {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send reset email to {}: {}", user.getEmail(), e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildResetEmailHtml(String fullName, String token) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Vistara Password Reset</title>
                  <style>
                    body {
                      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                      background-color: #f4f4f7;
                      margin: 0;
                      padding: 0;
                    }
                    .container {
                      max-width: 500px;
                      margin: 30px auto;
                      background: #ffffff;
                      border-radius: 16px;
                      box-shadow: 0 10px 25px rgba(0,0,0,0.05);
                      overflow: hidden;
                    }
                    .header {
                      background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
                      padding: 24px 20px;
                      text-align: center;
                      color: white;
                    }
                    .header h1 {
                      margin: 0;
                      font-size: 24px;
                      font-weight: 600;
                    }
                    .content {
                      padding: 32px 24px;
                    }
                    .greeting {
                      font-size: 18px;
                      font-weight: 500;
                      color: #1e2a3e;
                      margin-bottom: 16px;
                    }
                    .message {
                      color: #334155;
                      line-height: 1.5;
                      margin-bottom: 24px;
                    }
                    .token-box {
                      background: #f8fafc;
                      border: 1px solid #e2e8f0;
                      border-radius: 12px;
                      padding: 20px;
                      text-align: center;
                      margin-bottom: 24px;
                    }
                    .token {
                      font-size: 32px;
                      font-weight: 700;
                      letter-spacing: 4px;
                      color: #1e3c72;
                      font-family: monospace;
                      background: white;
                      display: inline-block;
                      padding: 8px 20px;
                      border-radius: 8px;
                      border: 1px solid #cbd5e1;
                    }
                    .expiry {
                      font-size: 14px;
                      color: #64748b;
                      margin-top: 12px;
                    }
                    .instruction {
                      background: #eff6ff;
                      border-left: 4px solid #3b82f6;
                      padding: 12px 16px;
                      font-family: monospace;
                      font-size: 14px;
                      color: #1e40af;
                      margin-bottom: 24px;
                      border-radius: 8px;
                      word-break: break-all;
                    }
                    .footer {
                      border-top: 1px solid #e2e8f0;
                      padding: 20px 24px;
                      font-size: 12px;
                      color: #94a3b8;
                      text-align: center;
                      background: #f9fafb;
                    }
                    .button {
                      background: #1e3c72;
                      color: white;
                      text-decoration: none;
                      padding: 10px 20px;
                      border-radius: 8px;
                      display: inline-block;
                      font-size: 14px;
                      font-weight: 500;
                    }
                  </style>
                </head>
                <body>
                  <div class="container">
                    <div class="header">
                      <h1>Vistara</h1>
                      <p>Tourist Safety & Tracking System</p>
                    </div>
                    <div class="content">
                      <div class="greeting">Hello %s,</div>
                      <div class="message">
                        We received a request to reset your Vistara account password.<br>
                        Use the 6‑digit code below to complete the process.
                      </div>
                      <div class="token-box">
                        <div class="token">%s</div>
                        <div class="expiry">⏱️ This code expires in 15 minutes</div>
                      </div>
                      <div class="instruction">
                        POST /api/v1/auth/reset-password<br>
                        {<br>
                        &nbsp;&nbsp;"token": "%s",<br>
                        &nbsp;&nbsp;"newPassword": "yourNewPassword"<br>
                        }
                      </div>
                      <div class="message" style="font-size:14px; color:#475569;">
                        If you didn't request this, you can safely ignore this email. Your password will not be changed.
                      </div>
                    </div>
                    <div class="footer">
                      &copy; 2026 Vistara – Keeping parks safe.<br>
                      This is an automated message, please do not reply.
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(fullName, token, token);
    }

    // -------------------------------------------------------
    // HTML email for password changed confirmation
    // -------------------------------------------------------
    private void sendPasswordChangedEmailHtml(User user) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(user.getEmail());
            helper.setSubject("✅ Vistara – Password Successfully Changed");

            String htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                      <meta charset="UTF-8">
                      <title>Password Changed</title>
                      <style>
                        body { font-family: 'Segoe UI', sans-serif; background: #f4f4f7; margin:0; padding:20px; }
                        .container { max-width:500px; margin:0 auto; background:white; border-radius:16px; padding:24px; box-shadow:0 5px 15px rgba(0,0,0,0.05); }
                        .header { background:linear-gradient(135deg,#1e3c72,#2a5298); color:white; text-align:center; padding:20px; border-radius:12px 12px 0 0; margin:-24px -24px 24px -24px; }
                        .content { line-height:1.6; color:#1e2a3e; }
                        .footer { margin-top:24px; font-size:12px; color:#94a3b8; text-align:center; border-top:1px solid #e2e8f0; padding-top:16px; }
                      </style>
                    </head>
                    <body>
                      <div class="container">
                        <div class="header"><h2>Vistara</h2></div>
                        <div class="content">
                          <p>Hello %s,</p>
                          <p>Your Vistara account password was <strong>successfully changed</strong>.</p>
                          <p>If you did not make this change, please contact our support team immediately.</p>
                          <p>— The Vistara Team</p>
                        </div>
                        <div class="footer">© 2026 Vistara – Tourist Safety & Tracking System</div>
                      </div>
                    </body>
                    </html>
                    """.formatted(user.getFullName());

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info("Password changed confirmation email sent to {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send password changed email to {}: {}", user.getEmail(), e.getMessage());
            log.error("Failed to send reset email to {}: ", user.getEmail(), e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}