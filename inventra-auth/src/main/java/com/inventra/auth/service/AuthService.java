package com.inventra.auth.service;

import com.inventra.auth.entity.User;
import com.inventra.auth.repository.UserRepository;
import com.inventra.auth.util.JWTUtility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    // ---------------- CHECK DUPLICATE EMAIL ----------------
    public String checkDuplicateEmail(String email) {
        try {
            User existingUser = userRepository.findByEmail(email);
            if (existingUser != null) {
                // Check if email is verified
                if (existingUser.isVerified()) {
                    return "Email already exists"; // User exists and is verified
                } else {
                    return "Email not verified"; // User exists but not verified - complete verification
                }
            }
            return "Email available";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error checking email";
        }
    }

    // ---------------- SEND OTP ----------------
    public String sendOtp(User user) {
        try {

            if (user.getEmail() == null || user.getPassword() == null) {
                return "Invalid data";
            }

            // Check duplicate email (including unverified users)
            User existingUser = userRepository.findByEmail(user.getEmail());
            if (existingUser != null) {
                if (existingUser.isVerified()) {
                    return "Email already exists";
                } else {
                    // Delete unverified user with same email - force fresh registration
                    userRepository.delete(existingUser);
                    System.out.println("\n⚠️  Deleted previous unverified account for: " + user.getEmail());
                }
            }

            // Encode password BEFORE saving
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            String otp = String.valueOf((int)(Math.random()*900000)+100000); // 6 digit OTP
            user.setOtp(otp);
            user.setVerified(false);

            // ⚠️ IMPORTANT: User is saved as UNVERIFIED - data will only be confirmed after OTP verification
            userRepository.save(user);

            // Log confirmation - data is TEMPORARY until verified
            System.out.println("\n================ OTP SENT - DATA NOT YET CONFIRMED ================");
            System.out.println("Username: " + user.getUsername());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Role: " + user.getRole());
            System.out.println("Verified: " + user.isVerified());
            System.out.println("OTP: " + otp + " (generated)");
            System.out.println("Status: ⏳ TEMPORARY DATA - Waiting for OTP verification");
            System.out.println("⚠️  If OTP is not verified, this account will be deleted and user must register again");
            System.out.println("====================================================================\n");

            // Try sending mail but do not crash
            try {
                emailService.sendOtp(user.getEmail(), otp);
            } catch (Exception e) {
                System.out.println("Mail failed: " + e.getMessage());
            }

            System.out.println("OTP for " + user.getEmail() + " is: " + otp); // visible in console

            return "OTP sent";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error in OTP";
        }
    }

    // ---------------- VERIFY OTP ----------------
    public String verifyOtp(String email, String otp) {
        try {
            User user = userRepository.findByEmail(email);

            if (user == null) {
                return "User not found. Please register again.";
            }

            if (user.getOtp() != null && user.getOtp().equals(otp)) {
                // ✅ OTP IS CORRECT - NOW PERMANENTLY STORE IN DATABASE
                user.setVerified(true);
                user.setOtp(null);  // Remove OTP from database
                
                // Save VERIFIED user to database - NOW DATA IS PERMANENT
                userRepository.save(user);
                
                // Log confirmation of permanent storage
                System.out.println("\n✅✅✅ EMAIL VERIFIED - DATA NOW STORED IN DATABASE ✅✅✅");
                System.out.println("Username: " + user.getUsername());
                System.out.println("Email: " + user.getEmail());
                System.out.println("Role: " + user.getRole());
                System.out.println("Verified: " + user.isVerified());
                System.out.println("OTP: CLEARED");
                System.out.println("Status: ✅ PERMANENTLY STORED - Account is now active!");
                System.out.println("==============================================================\n");
                
                return "Verified";
            }

            // ❌ OTP IS INCORRECT - DELETE THE UNVERIFIED USER
            System.out.println("\n❌ INVALID OTP ATTEMPT - Deleting unverified account: " + email);
            System.out.println("User must register again with correct OTP verification");
            userRepository.delete(user);  // Force user to register again
            System.out.println("==============================================================\n");

            return "Invalid OTP. Please register again.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error verifying OTP";
        }
    }

    // ---------------- LOGIN ----------------
    public String authenticateByEmail(String email, String password) {
        try {
            User user = userRepository.findByEmail(email);

            if (user == null) {
                return "Email not registered";
            }

            if (!user.isVerified()) {
                return "UNVERIFIED"; // Special marker for unverified user
            }

            if (passwordEncoder.matches(password, user.getPassword())) {
                return jwtUtility.generateToken(email);
            }

            return "Invalid password";

        } catch (Exception e) {
            e.printStackTrace();
            return "Login error";
        }
    }

    // ---------------- FORGOT PASSWORD ----------------
    public String processForgotPassword(String email) {
        try {
            User user = userRepository.findByEmail(email);

            if (user == null) return "Email not found";

            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            userRepository.save(user);

            try {
                emailService.sendResetLink(email, token);
            } catch (Exception e) {
                System.out.println("Mail failed: " + e.getMessage());
            }

            return "Reset link sent";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error in forgot password";
        }
    }

    // ---------------- RESET PASSWORD ----------------
    public String resetPassword(String token, String newPassword) {
        try {
            User user = userRepository.findByResetToken(token);

            if (user == null) return "Invalid token";

            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetToken(null);
            userRepository.save(user);

            return "Password reset successful";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error resetting password";
        }
    }
}
