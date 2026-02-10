package com.inventra.auth.service;

import com.inventra.auth.entity.User;
import com.inventra.auth.repository.UserRepository;
import com.inventra.auth.util.JWTUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JWTUtility jwtUtility;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Transactional
    public Map<String, Object> registerUser(String username, String email, String password) {
        Map<String, Object> response = new HashMap<>();

        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            response.put("success", false);
            response.put("message", "Username already exists");
            return response;
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            response.put("success", false);
            response.put("message", "Email already exists");
            return response;
        }

        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setVerified(false);

        // Generate OTP
        String otp = generateOTP();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));

        userRepository.save(user);

        // Send OTP email
        try {
            emailService.sendOtpEmail(email, otp);
            response.put("success", true);
            response.put("message", "Registration successful! Please check your email for OTP.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration successful but failed to send OTP email: " + e.getMessage());
        }

        return response;
    }

    @Transactional
    public Map<String, Object> verifyOtp(String email, String otp) {
        Map<String, Object> response = new HashMap<>();

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found");
            return response;
        }

        User user = userOpt.get();

        if (user.isVerified()) {
            response.put("success", false);
            response.put("message", "Email already verified");
            return response;
        }

        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            response.put("success", false);
            response.put("message", "Invalid OTP");
            return response;
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            response.put("success", false);
            response.put("message", "OTP has expired");
            return response;
        }

        // Verify user
        user.setVerified(true);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        // Send welcome email
        emailService.sendWelcomeEmail(email, user.getUsername());

        response.put("success", true);
        response.put("message", "Email verified successfully!");
        return response;
    }

    @Transactional
    public Map<String, Object> resendOtp(String email) {
        Map<String, Object> response = new HashMap<>();

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found");
            return response;
        }

        User user = userOpt.get();

        if (user.isVerified()) {
            response.put("success", false);
            response.put("message", "Email already verified");
            return response;
        }

        // Generate new OTP
        String otp = generateOTP();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        // Send OTP email
        try {
            emailService.sendOtpEmail(email, otp);
            response.put("success", true);
            response.put("message", "OTP resent successfully!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send OTP: " + e.getMessage());
        }

        return response;
    }

    public Map<String, Object> login(String username, String password) {
        Map<String, Object> response = new HashMap<>();

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Invalid username or password");
            return response;
        }

        User user = userOpt.get();

        if (!user.isVerified()) {
            response.put("success", false);
            response.put("message", "Please verify your email first");
            return response;
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            String token = jwtUtility.generateToken(username);

            response.put("success", true);
            response.put("message", "Login successful!");
            response.put("token", token);
            response.put("username", username);
            response.put("email", user.getEmail());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Invalid username or password");
        }

        return response;
    }

    @Transactional
    public Map<String, Object> forgotPassword(String email) {
        Map<String, Object> response = new HashMap<>();

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "No account found with this email");
            return response;
        }

        User user = userOpt.get();

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // Send reset email
        try {
            emailService.sendPasswordResetEmail(email, resetToken);
            response.put("success", true);
            response.put("message", "Password reset link sent to your email");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send reset email: " + e.getMessage());
        }

        return response;
    }

    @Transactional
    public Map<String, Object> resetPassword(String token, String newPassword) {
        Map<String, Object> response = new HashMap<>();

        Optional<User> userOpt = userRepository.findByResetToken(token);
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Invalid reset token");
            return response;
        }

        User user = userOpt.get();

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            response.put("success", false);
            response.put("message", "Reset token has expired");
            return response;
        }

        // Reset password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        response.put("success", true);
        response.put("message", "Password reset successfully!");
        return response;
    }

    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
