package com.inventra.auth.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    private String password;

    private String role; // ADMIN / MANAGER / STAFF

    @Column(name = "reset_token")
    private String resetToken;

    // OTP fields
    private String otp;
    private boolean verified;
}
