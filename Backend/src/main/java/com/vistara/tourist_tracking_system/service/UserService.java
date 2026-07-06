package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.AdminUpdateUserRequest;
import com.vistara.tourist_tracking_system.dto.RegisterRequest;
import com.vistara.tourist_tracking_system.dto.UpdateProfileRequest;
import com.vistara.tourist_tracking_system.dto.UserResponseDTO;
import com.vistara.tourist_tracking_system.exception.DuplicateResourceException;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmergencyService emergencyService;

    /**
     * Load user by username (email) for Spring Security
     * Maps the user's role to Spring Security authorities
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("🔍 Loading user by username: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("❌ User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        // Map user role to Spring Security authorities
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        String roleName = user.getRole().name();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));

        // Also add the role without ROLE_ prefix for convenience
        authorities.add(new SimpleGrantedAuthority(roleName));

        log.info("✅ User loaded: {} with authorities: {}", email, authorities);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isActive(),
                true,
                true,
                true,
                authorities
        );
    }

    /**
     * Register a new user with the specified role
     */
    @Transactional
    public User registerUser(RegisterRequest request, User.Role role) {
        log.info("📝 Registering new {} user: {}", role, request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("❌ Email already registered: {}", request.getEmail());
            throw new DuplicateResourceException("Email already registered");
        }

        // Check if national ID already exists
        if (request.getNationalId() != null && userRepository.existsByNationalId(request.getNationalId())) {
            log.warn("❌ National ID already registered: {}", request.getNationalId());
            throw new DuplicateResourceException("National ID already registered");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setNationalId(request.getNationalId());
        user.setRole(role);
        user.setEmergencyContactName(request.getEmergencyContactName());
        user.setEmergencyContactPhone(request.getEmergencyContactPhone());
        user.setActive(true);
        user.setVerified(false);

        User savedUser = userRepository.save(user);
        log.info("✅ User registered successfully with ID: {}", savedUser.getId());

        return savedUser;
    }

    /**
     * Find user by email
     */
    public User findByEmail(String email) {
        log.debug("🔍 Finding user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("❌ User not found with email: {}", email);
                    return new DuplicateResourceException("User not found");
                });
    }

    /**
     * Find user by ID
     */
    public User findById(Long id) {
        log.debug("🔍 Finding user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ User not found with ID: {}", id);
                    return new RuntimeException("User not found with id: " + id);
                });
    }

    /**
     * Check if user exists by email
     */
    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Check if user exists by national ID
     */
    public boolean userExistsByNationalId(String nationalId) {
        return userRepository.existsByNationalId(nationalId);
    }

    /**
     * Create a tourist user with temporary password
     */
    @Transactional
    public User createTourist(String fullName, String email, String phoneNumber) {
        log.info("📝 Creating tourist user: {}", email);

        if (userRepository.existsByEmail(email)) {
            log.warn("❌ User with email already exists: {}", email);
            throw new RuntimeException("User with email " + email + " already exists");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setRole(User.Role.TOURIST);
        user.setActive(true);
        user.setVerified(true);

        // Generate temporary password
        String tempPassword = UUID.randomUUID().toString().substring(0, 12);
        user.setPassword(passwordEncoder.encode(tempPassword));

        User savedUser = userRepository.save(user);
        log.info("✅ Tourist user created with ID: {}", savedUser.getId());

        return savedUser;
    }

    /**
     * Find or create a tourist user
     */
    @Transactional
    public User findOrCreateTourist(String fullName, String email, String phoneNumber) {
        log.debug("🔍 Finding or creating tourist: {}", email);

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            log.debug("✅ Found existing tourist: {}", email);

            // Update fields if provided
            if (fullName != null && !fullName.isBlank()) {
                user.setFullName(fullName);
            }
            if (phoneNumber != null && !phoneNumber.isBlank()) {
                user.setPhoneNumber(phoneNumber);
            }
            return userRepository.save(user);
        } else {
            log.info("📝 Creating new tourist: {}", email);
            return createTourist(fullName, email, phoneNumber);
        }
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserResponseDTO updateProfile(String currentEmail, UpdateProfileRequest request) {
        log.info("📝 Updating profile for user: {}", currentEmail);

        User user = findByEmail(currentEmail);

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!request.getEmail().equals(currentEmail) && userRepository.existsByEmail(request.getEmail())) {
                log.warn("❌ Email already in use: {}", request.getEmail());
                throw new RuntimeException("Email already in use by another account");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getEmergencyContactName() != null) {
            user.setEmergencyContactName(request.getEmergencyContactName());
        }
        if (request.getEmergencyContactPhone() != null) {
            user.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }

        User saved = userRepository.save(user);
        log.info("✅ Profile updated for user: {}", saved.getEmail());

        return convertToDTO(saved);
    }

    /**
     * Convert User entity to UserResponseDTO
     */
    private UserResponseDTO convertToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());
        dto.setNationalId(user.getNationalId());
        dto.setEmergencyContactName(user.getEmergencyContactName());
        dto.setEmergencyContactPhone(user.getEmergencyContactPhone());
        dto.setActive(user.isActive());
        dto.setVerified(user.isVerified());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    /**
     * Get all users
     */
    public List<UserResponseDTO> getAllUsers() {
        log.debug("🔍 Fetching all users");
        List<User> users = userRepository.findAllByOrderByIdAsc();
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user profile by email
     */
    public UserResponseDTO getUserProfile(String email) {
        log.debug("🔍 Fetching profile for: {}", email);
        User user = findByEmail(email);
        return convertToDTO(user);
    }

    /**
     * Get user by ID
     */
    public UserResponseDTO getUserById(Long userId) {
        log.debug("🔍 Fetching user by ID: {}", userId);
        User user = findById(userId);
        return convertToDTO(user);
    }

    /**
     * Admin update user
     */
    @Transactional
    public UserResponseDTO adminUpdateUser(Long userId, AdminUpdateUserRequest request) {
        log.info("📝 Admin updating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("❌ User not found with ID: {}", userId);
                    return new RuntimeException("User not found");
                });

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                log.warn("❌ Email already in use: {}", request.getEmail());
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getEmergencyContactName() != null) {
            user.setEmergencyContactName(request.getEmergencyContactName());
        }
        if (request.getEmergencyContactPhone() != null) {
            user.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }
        if (request.getActive() != null) user.setActive(request.getActive());
        if (request.getVerified() != null) user.setVerified(request.getVerified());

        User saved = userRepository.save(user);
        log.info("✅ User updated by admin: {}", saved.getEmail());

        return convertToDTO(saved);
    }

    /**
     * Delete user (soft delete)
     */
    @Transactional
    public void deleteUser(Long userId) {
        log.info("🗑️ Deleting user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("❌ User not found with ID: {}", userId);
                    return new RuntimeException("User not found");
                });

        // Soft delete - set active to false
        user.setActive(false);
        userRepository.save(user);
        log.info("✅ User soft deleted: {}", userId);
    }

    /**
     * Activate/deactivate user
     */
    @Transactional
    public UserResponseDTO setUserActive(Long userId, boolean active) {
        log.info("📝 Setting user {} active status to: {}", userId, active);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("❌ User not found with ID: {}", userId);
                    return new RuntimeException("User not found");
                });

        user.setActive(active);
        User saved = userRepository.save(user);
        log.info("✅ User active status updated: {}", userId);

        return convertToDTO(saved);
    }

    // ===== Ranger Management Methods =====

    /**
     * Get all rangers
     */
    public List<User> getRangers() {
        log.debug("🔍 Fetching all rangers");
        return userRepository.findByRole(User.Role.RANGER);
    }

    /**
     * Get all admins
     */
    public List<User> getAdmins() {
        log.debug("🔍 Fetching all admins");
        return userRepository.findByRole(User.Role.ADMIN);
    }

    /**
     * Get all tourists
     */
    public List<User> getTourists() {
        log.debug("🔍 Fetching all tourists");
        return userRepository.findByRole(User.Role.TOURIST);
    }

    /**
     * Get all rangers as DTOs
     */
    public List<UserResponseDTO> getAllRangers() {
        log.debug("🔍 Fetching all rangers as DTOs");
        List<User> rangers = userRepository.findByRole(User.Role.RANGER);
        return rangers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get available rangers (not assigned to any active alert)
     */
    public List<UserResponseDTO> getAvailableRangers() {
        log.debug("🔍 Fetching available rangers");

        List<User> rangers = userRepository.findByRole(User.Role.RANGER);
        List<User> availableRangers = rangers.stream()
                .filter(ranger -> {
                    boolean hasActiveAlert = emergencyService.hasActiveAlertAssigned(ranger.getId());
                    log.debug("Ranger {} has active alert: {}", ranger.getEmail(), hasActiveAlert);
                    return !hasActiveAlert && ranger.isActive();
                })
                .collect(Collectors.toList());

        log.info("✅ Found {} available rangers out of {}", availableRangers.size(), rangers.size());

        return availableRangers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get rangers by status
     */
    public List<UserResponseDTO> getRangersByStatus(boolean active) {
        log.debug("🔍 Fetching rangers with status: {}", active);

        List<User> rangers = userRepository.findByRoleAndActive(User.Role.RANGER, active);
        return rangers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update ranger's active status
     */
    @Transactional
    public UserResponseDTO updateRangerStatus(Long rangerId, boolean active) {
        log.info("📝 Updating ranger {} status to: {}", rangerId, active);

        User ranger = userRepository.findById(rangerId)
                .orElseThrow(() -> {
                    log.warn("❌ Ranger not found with ID: {}", rangerId);
                    return new RuntimeException("Ranger not found with id: " + rangerId);
                });

        if (ranger.getRole() != User.Role.RANGER) {
            log.warn("❌ User {} is not a ranger", rangerId);
            throw new RuntimeException("User is not a ranger");
        }

        ranger.setActive(active);
        User saved = userRepository.save(ranger);
        log.info("✅ Ranger status updated: {}", saved.getEmail());

        return convertToDTO(saved);
    }

    /**
     * Get ranger statistics
     */
    public UserResponseDTO getRangerStats(Long rangerId) {
        log.debug("🔍 Fetching ranger stats for: {}", rangerId);

        User ranger = userRepository.findById(rangerId)
                .orElseThrow(() -> {
                    log.warn("❌ Ranger not found with ID: {}", rangerId);
                    return new RuntimeException("Ranger not found");
                });

        if (ranger.getRole() != User.Role.RANGER) {
            log.warn("❌ User {} is not a ranger", rangerId);
            throw new RuntimeException("User is not a ranger");
        }

        // Get additional stats from emergency service
        boolean hasActiveAlert = emergencyService.hasActiveAlertAssigned(rangerId);

        UserResponseDTO dto = convertToDTO(ranger);
        // You could add additional fields to UserResponseDTO for stats
        // or return a separate stats object

        return dto;
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        log.info("📝 Changing password for user: {}", email);

        User user = findByEmail(email);

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("❌ Invalid current password for user: {}", email);
            throw new RuntimeException("Current password is incorrect");
        }

        // Encode and set new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("✅ Password changed for user: {}", email);
    }

    /**
     * Reset user password (admin only)
     */
    @Transactional
    public String resetPassword(Long userId) {
        log.info("📝 Resetting password for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("❌ User not found with ID: {}", userId);
                    return new RuntimeException("User not found");
                });

        // Generate new temporary password
        String tempPassword = UUID.randomUUID().toString().substring(0, 12);
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        log.info("✅ Password reset for user: {}", userId);
        return tempPassword;
    }
}