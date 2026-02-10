package com.inventra.auth.controller;

import com.inventra.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class AuthPageController {

    @Autowired
    private AuthService authService;

    // Page Routes
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(@RequestParam(required = false) String email, Model model) {
        model.addAttribute("email", email);
        return "verify-otp";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @GetMapping("/home")
    public String homePage() {
        return "home";
    }

    // API Endpoints
    @PostMapping("/api/auth/signup")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");

        Map<String, Object> response = authService.registerUser(username, email, password);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/auth/verify-otp")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        Map<String, Object> response = authService.verifyOtp(email, otp);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/auth/resend-otp")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        Map<String, Object> response = authService.resendOtp(email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        Map<String, Object> response = authService.login(username, password);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/auth/forgot-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        Map<String, Object> response = authService.forgotPassword(email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/auth/reset-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String password = request.get("password");

        Map<String, Object> response = authService.resetPassword(token, password);
        return ResponseEntity.ok(response);
    }
}
