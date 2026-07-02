package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.dto.AdminUpdateUserRequest;
import com.vistara.tourist_tracking_system.dto.RegisterRequest;
import com.vistara.tourist_tracking_system.dto.UpdateProfileRequest;
import com.vistara.tourist_tracking_system.dto.UserResponseDTO;
import com.vistara.tourist_tracking_system.exception.DuplicateResourceException;
import com.vistara.tourist_tracking_system.model.User;
import com.vistara.tourist_tracking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmergencyService emergencyService;  // Added this

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public User registerUser(RegisterRequest request, User.Role role) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        if (request.getNationalId() != null && userRepository.existsByNationalId(request.getNationalId())) {
            throw new DuplicateResourceException("National ID already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setNationalId(request.getNationalId());
        user.setRole(role);
        user.setEmergencyContactName(request.getEmergencyContactName());
        user.setEmergencyContactPhone(request.getEmergencyContactPhone());

        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new DuplicateResourceException("User not found"));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Transactional
    public User createTourist(String fullName, String email, String phoneNumber) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User with email " + email + " already exists");
        }
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setRole(User.Role.TOURIST);
        user.setActive(true);
        user.setVerified(true);
        String tempPassword = UUID.randomUUID().toString();
        user.setPassword(passwordEncoder.encode(tempPassword));
        return userRepository.save(user);
    }

    @Transactional
    public User findOrCreateTourist(String fullName, String email, String phoneNumber) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (fullName != null && !fullName.isBlank()) {
                user.setFullName(fullName);
            }
            if (phoneNumber != null && !phoneNumber.isBlank()) {
                user.setPhoneNumber(phoneNumber);
            }
            return userRepository.save(user);
        } else {
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhoneNumber(phoneNumber);
            user.setRole(User.Role.TOURIST);
            user.setActive(true);
            user.setVerified(true);
            String tempPassword = UUID.randomUUID().toString();
            user.setPassword(passwordEncoder.encode(tempPassword));
            return userRepository.save(user);
        }
    }

    @Transactional
    public UserResponseDTO updateProfile(String currentEmail, UpdateProfileRequest request) {
        User user = findByEmail(currentEmail);

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!request.getEmail().equals(currentEmail) && userRepository.existsByEmail(request.getEmail())) {
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
        return convertToDTO(saved);
    }

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
        return dto;
    }

    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAllByOrderByIdAsc();
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserProfile(String email) {
        User user = findByEmail(email);
        return convertToDTO(user);
    }

    public UserResponseDTO getUserById(Long userId) {
        User user = findById(userId);
        return convertToDTO(user);
    }

    @Transactional
    public UserResponseDTO adminUpdateUser(Long userId, AdminUpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getEmergencyContactName() != null) user.setEmergencyContactName(request.getEmergencyContactName());
        if (request.getEmergencyContactPhone() != null) user.setEmergencyContactPhone(request.getEmergencyContactPhone());
        if (request.getActive() != null) user.setActive(request.getActive());
        if (request.getVerified() != null) user.setVerified(request.getVerified());

        return convertToDTO(userRepository.save(user));
    }

    // Ranger Management Methods
    public List<User> getRangers() {
        return userRepository.findByRole(User.Role.RANGER);
    }

    public List<User> getAdmins() {
        return userRepository.findByRole(User.Role.ADMIN);
    }

    public List<User> getTourists() {
        return userRepository.findByRole(User.Role.TOURIST);
    }

    public List<UserResponseDTO> getAllRangers() {
        List<User> rangers = userRepository.findByRole(User.Role.RANGER);
        return rangers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UserResponseDTO> getAvailableRangers() {
        List<User> rangers = userRepository.findByRole(User.Role.RANGER);
        List<User> availableRangers = rangers.stream()
                .filter(ranger -> !emergencyService.hasActiveAlertAssigned(ranger.getId()))
                .collect(Collectors.toList());
        return availableRangers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}