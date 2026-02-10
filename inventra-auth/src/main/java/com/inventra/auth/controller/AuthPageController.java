package com.inventra.auth.controller;

import com.inventra.auth.entity.User;
import com.inventra.auth.service.AuthService;
import com.inventra.auth.repository.UserRepository;
import com.inventra.auth.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthPageController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // ================= LOGIN PAGE =================
    @GetMapping("/")
    public String loginPage() {
        return "login";
    }

    // ================= SIGNUP PAGE =================
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    // ================= FORGOT PAGE =================
    @GetMapping("/forgot")
    public String forgotPage() {
        return "forgot-password";
    }

    // ================= SEND OTP =================
    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam String username,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam String confirmPassword,
                          @RequestParam String role,
                          RedirectAttributes redirectAttributes) {

        // Validate email format (@gmail.com only)
        if (!email.endsWith("@gmail.com")) {
            redirectAttributes.addFlashAttribute("error", "Email must end with @gmail.com");
            return "redirect:/signup";
        }

        // Validate password length
        if (password.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 8 characters long");
            return "redirect:/signup";
        }

        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match!");
            return "redirect:/signup";
        }

        // Check for duplicate email
        String result = authService.checkDuplicateEmail(email);
        if (result.equals("Email already exists")) {
            // Email is verified - cannot use this email
            redirectAttributes.addFlashAttribute("error", "Email already registered! Please use a different email.");
            redirectAttributes.addFlashAttribute("clearForm", true);
            return "redirect:/signup";
        } else if (result.equals("Email not verified")) {
            // Email exists but not verified - user should complete verification instead
            redirectAttributes.addFlashAttribute("error", "⚠️ This email is pending verification. Please verify your email first to complete registration.");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/verify-otp-page";
        }

        // Create user object
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);

        // Send OTP
        String otpResult = authService.sendOtp(user);
        
        if (otpResult.equals("OTP sent")) {
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("success", "✅ Step 1 Complete! 6-digit verification code sent to " + email);
            return "redirect:/verify-otp-page";
        }

        redirectAttributes.addFlashAttribute("error", "Failed to send OTP");
        return "redirect:/signup";
    }

    // ================= VERIFY OTP PAGE =================
    @GetMapping("/verify-otp-page")
    public String verifyOtpPage(Model model) {
        return "verify-otp";
    }

    // ================= VERIFY OTP =================
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email,
                            @RequestParam String otp,
                            RedirectAttributes redirectAttributes) {

        String result = authService.verifyOtp(email, otp);

        if (result.equals("Verified")) {
            redirectAttributes.addFlashAttribute("success", "🎉 Step 2 Complete! Email verified successfully. Your account is now active. Please login!");
            return "redirect:/";
        }

        // Show appropriate error message
        if (result.equals("Invalid OTP. Please register again.")) {
            redirectAttributes.addFlashAttribute("error", "❌ Wrong OTP! Your registration has been cleared. Please register again.");
            return "redirect:/signup";
        }

        redirectAttributes.addFlashAttribute("error", result);
        redirectAttributes.addFlashAttribute("email", email);
        return "redirect:/verify-otp-page";
    }

    // ================= LOGIN =================
    @PostMapping("/do-login")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          RedirectAttributes redirectAttributes) {

        String token = authService.authenticateByEmail(email, password);

        if (token.startsWith("ey")) {
            // Successful login - redirect to home
            return "home";
        }

        // Check if user is unverified
        if (token.equals("UNVERIFIED")) {
            // Send new OTP to unverified email
            User user = userRepository.findByEmail(email);
            if (user != null) {
                // Generate new OTP
                String otp = String.valueOf((int)(Math.random()*900000)+100000);
                user.setOtp(otp);
                userRepository.save(user);
                
                // Send OTP via email
                try {
                    emailService.sendOtp(email, otp);
                    System.out.println("\n📧 NEW OTP SENT TO UNVERIFIED EMAIL: " + email);
                    System.out.println("OTP: " + otp);
                    System.out.println("========================================\n");
                } catch (Exception e) {
                    System.out.println("Mail failed: " + e.getMessage());
                }
                
                // Redirect to verify page with email
                redirectAttributes.addFlashAttribute("email", email);
                redirectAttributes.addFlashAttribute("success", "⏳ Your account is pending email verification. A new OTP has been sent to " + email);
                return "redirect:/verify-otp-page";
            }
        }

        // Failed login
        redirectAttributes.addFlashAttribute("error", token);
        return "redirect:/";
    }

    // ================= FORGOT PASSWORD =================
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email,
                                  RedirectAttributes redirectAttributes) {

        String result = authService.processForgotPassword(email);

        if (result.equals("Reset link sent")) {
            redirectAttributes.addFlashAttribute("success", "Password reset link sent to your email!");
        } else if (result.equals("Email not found")) {
            redirectAttributes.addFlashAttribute("error", "Email not registered!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to send reset link");
        }

        return "redirect:/forgot";
    }

    // ================= RESET PASSWORD PAGE =================
    @GetMapping("/reset-password/{token}")
    public String resetPasswordPage(@PathVariable String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    // ================= RESET PASSWORD =================
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                RedirectAttributes redirectAttributes) {

        // Validate password confirmation
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match!");
            return "redirect:/reset-password/" + token;
        }

        String result = authService.resetPassword(token, newPassword);

        if (result.equals("Password reset successful")) {
            redirectAttributes.addFlashAttribute("success", "Password reset successfully! Please login.");
            return "redirect:/";
        }

        redirectAttributes.addFlashAttribute("error", "Invalid or expired reset link");
        return "redirect:/reset-password/" + token;
    }
}
