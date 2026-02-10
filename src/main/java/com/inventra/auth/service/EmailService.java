package com.inventra.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Inventra - Email Verification OTP");
            message.setText("Your OTP for email verification is: " + otp + "\n\n"
                    + "This OTP is valid for 10 minutes.\n\n"
                    + "If you didn't request this, please ignore this email.");
            
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Inventra - Password Reset Request");
            message.setText("Click the link below to reset your password:\n\n"
                    + "http://localhost:8080/reset-password?token=" + resetToken + "\n\n"
                    + "This link is valid for 1 hour.\n\n"
                    + "If you didn't request this, please ignore this email.");
            
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send password reset email: " + e.getMessage());
        }
    }

    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Inventra!");
            message.setText("Hello " + username + ",\n\n"
                    + "Welcome to Inventra! Your account has been successfully verified.\n\n"
                    + "Thank you for joining us!");
            
            mailSender.send(message);
        } catch (Exception e) {
            // Don't throw exception for welcome email failures
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }
}
