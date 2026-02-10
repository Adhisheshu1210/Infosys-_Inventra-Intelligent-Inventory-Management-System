package com.inventra.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtp(String email, String otp) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Inventra - Email Verification OTP");
        msg.setText("Hello,\n\nYour OTP for email verification is: " + otp + "\n\nThis OTP is valid for 10 minutes.\n\nIf you didn't request this, please ignore this email.\n\nRegards,\nInventra Team");
        mailSender.send(msg);
    }

    public void sendResetLink(String email, String token) {
        String link = "http://localhost:8080/reset-password/" + token;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Inventra - Reset Password");
        msg.setText("Hello,\n\nClick the link below to reset your password:\n\n" + link + "\n\nThis link will expire in 24 hours.\n\nIf you didn't request this, please ignore this email.\n\nRegards,\nInventra Team");
        mailSender.send(msg);
    }
}
